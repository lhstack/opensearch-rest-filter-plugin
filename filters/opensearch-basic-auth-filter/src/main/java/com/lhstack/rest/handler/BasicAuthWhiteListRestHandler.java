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
