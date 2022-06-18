package com.lhstack.opensearch.filters;

import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.filter.RestFilterChain;
import com.lhstack.opensearch.filter.RestFilterContext;
import org.opensearch.rest.RestChannel;
import org.opensearch.rest.RestRequest;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 10:23
 * @Modify by
 */
public class TestRestFilter3 implements RestFilter {

    @Override
    public void filter(RestRequest request, RestChannel restChannel, RestFilterContext context, RestFilterChain filterChain) throws Exception {
        System.out.println("3");
        context.setAttribute("hello", "world");
        context.setAttribute("message", 1);
        filterChain.doFilter(request, restChannel, context);
    }

    @Override
    public int ordered() {
        return 2;
    }
}
