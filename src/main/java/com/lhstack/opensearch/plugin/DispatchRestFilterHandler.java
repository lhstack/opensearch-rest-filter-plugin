package com.lhstack.opensearch.plugin;

import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.filter.RestFilterChain;
import org.opensearch.client.node.NodeClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.rest.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 10:12
 * @Modify by
 */
public class DispatchRestFilterHandler implements RestHandler {

    private final List<RestFilter> restFilters;
    private final Settings settings;
    private final RestHandler restHandler;
    private final ThreadContext threadContext;

    public DispatchRestFilterHandler(RestHandler restHandler, ThreadContext threadContext, Settings settings, List<RestFilter> restFilters) {
        this.restFilters = restFilters;
        this.settings = settings;
        this.restHandler = restHandler;
        this.threadContext = threadContext;
    }

    @Override
    public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        RestFilterChain restFilterChain = new RestFilterChain() {
            private int index;

            @Override
            public void doFilter(RestRequest request, Map<String, Object> context, ThreadContext threadContext, RestChannel channel, NodeClient nodeClient, Settings settings) {
                try {
                    if (index < restFilters.size()) {
                        restFilters.get(index++).filter(request, context, threadContext, channel, client, settings, this);
                    } else {
                        restHandler.handleRequest(request, channel, nodeClient);
                    }
                } catch (Exception e) {
                    BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, "application/json;charset=UTF-8",
                            String.format("{\"message\":\"%s\",\"state\":400,\"status\":\"failure\"}", e.getMessage()));
                    channel.sendResponse(bytesRestResponse);
                }
            }
        };
        restFilterChain.doFilter(request, new HashMap<>(1), this.threadContext, channel, client, this.settings);
    }
}
