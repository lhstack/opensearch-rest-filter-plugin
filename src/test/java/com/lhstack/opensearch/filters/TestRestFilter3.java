package com.lhstack.opensearch.filters;

import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.filter.RestFilterChain;
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
 * @Date 2022/6/16 10:23
 * @Modify by
 */
public class TestRestFilter3 implements RestFilter {
    @Override
    public void filter(RestRequest request, Map<String, Object> context, ThreadContext threadContext, RestChannel channel, NodeClient nodeClient, Settings settings, RestFilterChain filterChain) throws Exception {
        System.out.println("3");
        filterChain.doFilter(request, context, threadContext, channel, nodeClient, settings);
    }

    @Override
    public int ordered() {
        return 2;
    }
}
