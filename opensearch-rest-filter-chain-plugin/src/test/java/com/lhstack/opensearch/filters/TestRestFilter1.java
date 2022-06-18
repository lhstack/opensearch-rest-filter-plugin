package com.lhstack.opensearch.filters;

import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.filter.RestFilterChain;
import com.lhstack.opensearch.filter.RestFilterContext;
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
public class TestRestFilter1 implements RestFilter {


    @Override
    public void filter(RestRequest request, RestChannel restChannel, RestFilterContext context, RestFilterChain filterChain) throws Exception {
        System.out.println("1");
        String hello = context.getAttribute("hello");
        int message = context.getAttribute("message");
        String notfound = context.getAttribute("notfound");
        String attribute = context.getAttribute("defaultValue", "defaultValue");
        System.out.println(hello);
        System.out.println(message);
        System.out.println(notfound);
        System.out.println(attribute);
        System.out.println("1");
        filterChain.doFilter(request,restChannel,context);
    }

    @Override
    public int ordered() {
        return 1;
    }
}
