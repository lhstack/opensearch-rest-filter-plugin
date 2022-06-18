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
