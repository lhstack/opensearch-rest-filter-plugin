package com.lhstack.opensearch;

import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.plugin.DispatchRestFilterHandler;
import com.lhstack.opensearch.util.SpiUtils;
import org.junit.jupiter.api.Test;
import org.opensearch.common.settings.Settings;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 10:24
 * @Modify by
 */
class RestFilterTests {
    @Test
    void test() throws Exception {
        List<RestFilter> restFilters = SpiUtils.loadClassToInstance(RestFilter.class)
                .stream()
                .sorted(Comparator.comparing(item -> -item.ordered()))
                .collect(Collectors.toList());
        DispatchRestFilterHandler dispatchRestFilterHandler = new DispatchRestFilterHandler(null, null, Settings.EMPTY, restFilters);
        dispatchRestFilterHandler.handleRequest(null, null, null);
        dispatchRestFilterHandler.handleRequest(null, null, null);
    }

}
