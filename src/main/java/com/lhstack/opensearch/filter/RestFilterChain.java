package com.lhstack.opensearch.filter;

import org.opensearch.client.node.NodeClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.rest.RestChannel;
import org.opensearch.rest.RestRequest;

import java.util.Map;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 9:52
 * @Modify by
 */
public interface RestFilterChain {

    /**
     * 调度过滤链
     *
     * @param request
     * @param context
     * @param threadContext
     * @param channel
     * @param nodeClient
     * @param settings
     */
    void doFilter(RestRequest request, Map<String, Object> context, ThreadContext threadContext, RestChannel channel, NodeClient nodeClient, Settings settings);
}
