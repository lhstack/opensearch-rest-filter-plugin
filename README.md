## 基于es的过滤器功能
- 如自定义过滤器中需要用到特权，修改此插件对应的plugin-security.policy设置对应权限即可
- 自定义过滤器需要在maven中依赖此插件和opensearch依赖
```xml
    <dependencies>
        <dependency>
            <groupId>com.lhstack.opensearch</groupId>
            <artifactId>opensearch-rest-filter-chain-plugin</artifactId>
            <version>2.0.0</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.opensearch</groupId>
            <artifactId>opensearch</artifactId>
            <version>2.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.83</version>
        </dependency>
</dependencies>
```
- 在resources目录下添加META-INF/com.lhstack.opensearch.filter.RestFilter文件
![img1.png](images/img1.png)
![img2.png](images/img2.png)
- 内容如下
```java
package com.lhstack.filter;

import com.alibaba.fastjson.JSONObject;
import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.filter.RestFilterChain;
import com.lhstack.opensearch.filter.RestFilterContext;
import com.lhstack.opensearch.handler.DocumentRestHandler;
import com.lhstack.opensearch.util.PrivilegedUtils;
import com.lhstack.rest.handler.BasicAuthWhiteListRestHandler;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.common.settings.*;
import org.opensearch.rest.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/18 18:24
 * @Modify By
 */
public class BasicAuthRestFilter implements RestFilter {

    private static final String USERNAME_PROPERTY_KEY = "basic.auth.username";
    private static final String PASSWORD_PROPERTY_KEY = "basic.auth.password";
    private static final String ENABLE_PROPERTY_KEY = "basic.auth.enable";
    private static final String WHITE_LIST_PROPERTY_KEY = "basic.auth.white-list";

    private static final String USERNAME_ENV_KEY = "BASIC_AUTH_USERNAME";
    private static final String PASSWORD_ENV_KEY = "BASIC_AUTH_PASSWORD";
    private static final String ENABLE_ENV_KEY = "BASIC_AUTH_ENABLE";
    private static final String WHITE_LIST_ENV_KEY = "BASIC_AUTH_WHITE_LIST";
    private String username;
    private String password;
    private Boolean enable;
    private Set<String> whiteList;


    @Override
    public void filter(RestRequest request, RestChannel restChannel, RestFilterContext context, RestFilterChain filterChain) throws Exception {
        if (!this.enable || hasWhiteList(request.getHttpChannel().getRemoteAddress())) {
            filterChain.doFilter(request, restChannel, context);
            return;
        }
        String authorization = request.header("Authorization");
        if (Objects.isNull(authorization) || authorization.isEmpty()) {
            writeBad(restChannel, "请开启Basic认证，并输入正确的用户名和密码");
            return;
        }
        authorization = authorization.startsWith("Basic") ? authorization.substring(6) : authorization;
        byte[] decode = Base64.getDecoder().decode(authorization);
        String upass = new String(decode, StandardCharsets.UTF_8);
        String[] upassArray = upass.split(":");
        if (upassArray.length != 2) {
            writeBad(restChannel, "请输入完整的用户名和密码，并且时Basic认证方式");
            return;
        }
        String iptUser = upassArray[0];
        String iptPass = upassArray[1];
        if (iptUser.equals(username) && iptPass.equals(password)) {
            filterChain.doFilter(request, restChannel, context);
            return;
        }
        writeBad(restChannel, "用户名和密码不正确");
    }

    private void writeBad(RestChannel channel, String message) {
        String result = PrivilegedUtils.doPrivileged(() -> JSONObject.toJSONString(Map.of("state", 401, "status", "unauthorized", "message", message)));
        RestResponse restResponse = new BytesRestResponse(RestStatus.UNAUTHORIZED, "application/json;charset=UTF-8", result);
        restResponse.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s \"", message));
        channel.sendResponse(restResponse);
    }

    private boolean hasWhiteList(InetSocketAddress remoteAddress) {
        return this.whiteList.contains(remoteAddress.getHostName()) || this.whiteList.stream().anyMatch(item -> Pattern.matches(item, remoteAddress.getHostName()));
    }

    @Override
    public int ordered() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void init(Settings settings, DocumentRestHandler documentRestHandler, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
        Map<String, String> env = System.getenv();
        this.username = settings.get(USERNAME_PROPERTY_KEY, System.getProperty(USERNAME_PROPERTY_KEY, env.getOrDefault(USERNAME_ENV_KEY, "admin")));
        this.password = settings.get(PASSWORD_PROPERTY_KEY, System.getProperty(PASSWORD_PROPERTY_KEY, env.getOrDefault(PASSWORD_ENV_KEY, "admin")));
        this.enable = settings.getAsBoolean(ENABLE_PROPERTY_KEY, Boolean.parseBoolean(System.getProperty(ENABLE_ENV_KEY, env.getOrDefault(ENABLE_ENV_KEY, "true"))));
        String whiteListString = settings.get(WHITE_LIST_PROPERTY_KEY, System.getProperty(WHITE_LIST_PROPERTY_KEY, env.getOrDefault(WHITE_LIST_ENV_KEY, "")));
        this.whiteList = Arrays.stream(whiteListString.split(",")).collect(Collectors.toSet());
        restController.registerHandler(new BasicAuthWhiteListRestHandler(documentRestHandler, whiteList));
    }

    @Override
    public void initSettingsDefinition(Set<Setting<?>> settings) {
        settings.add(Setting.simpleString(USERNAME_PROPERTY_KEY, "admin", Setting.Property.NodeScope));
        settings.add(Setting.simpleString(PASSWORD_PROPERTY_KEY, "admin", Setting.Property.NodeScope));
        settings.add(Setting.boolSetting(ENABLE_PROPERTY_KEY, true, Setting.Property.NodeScope));
        settings.add(Setting.simpleString(WHITE_LIST_PROPERTY_KEY, "", Setting.Property.NodeScope));
    }
}

```
```java
package com.lhstack.rest.handler;

import com.alibaba.fastjson.JSONObject;
import com.lhstack.opensearch.handler.DocumentRestHandler;
import com.lhstack.opensearch.util.PrivilegedUtils;
import org.opensearch.client.node.NodeClient;
import org.opensearch.rest.*;

import java.util.*;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/18 18:36
 * @Modify By
 */
public class BasicAuthWhiteListRestHandler implements RestHandler {
    private final Set<String> whiteList;

    public BasicAuthWhiteListRestHandler(DocumentRestHandler documentRestHandler, Set<String> whiteList) {
        this.whiteList = whiteList;
        documentRestHandler.addDocuments("GET /_basic_auth/whiteList 查看当前白名单");
        documentRestHandler.addDocuments("POST /_basic_auth/whiteList?whiteList=xxx,zzz 更新白名单设置，支持正则表达式，多个通过,隔开");
    }

    public RestResponse buildRestResponse(Object data) {
        return PrivilegedUtils.doPrivileged(() -> new BytesRestResponse(RestStatus.OK, "application/json;charset=utf-8", JSONObject.toJSONString(data)));
    }

    @Override
    public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        RestRequest.Method method = request.method();
        RestResponse restResponse;
        if (method == RestRequest.Method.GET) {
            restResponse = buildRestResponse(this.whiteList);
        } else if (method == RestRequest.Method.POST) {
            String whiteListString = request.param("whiteList");
            if (Objects.nonNull(whiteListString) && !whiteListString.isBlank()) {
                String[] whiteListArray = whiteListString.split(",");
                this.whiteList.clear();
                this.whiteList.addAll(Arrays.asList(whiteListArray));
                restResponse = buildRestResponse(this.whiteList);
            } else {
                restResponse = buildRestResponse(Map.of("message", "请在url上添加whiteList参数，设置白名单", "existWhiteList", this.whiteList));
            }
        } else {
            restResponse = buildRestResponse(Map.of("message", "Only GET and POST methods are supported"));
        }
        channel.sendResponse(restResponse);
    }

    @Override
    public List<Route> routes() {
        return Arrays.asList(
                new Route(RestRequest.Method.GET, "/_basic_auth/whiteList"),
                new Route(RestRequest.Method.POST, "/_basic_auth/whiteList"));
    }
}

```

![image-20220617094454633](images/3.png)

![image-20220617094534785](images/4.png)

![image-20220617094646228](images/5.png)

![image-20220617094717578](images/6.png)