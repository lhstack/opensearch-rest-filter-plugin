package com.lhstack.opensearch.plugin;

import com.lhstack.opensearch.filter.RestFilter;
import com.lhstack.opensearch.handler.DocumentRestHandler;
import com.lhstack.opensearch.util.SpiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.common.inject.Inject;
import org.opensearch.common.settings.*;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 10:10
 * @Modify by
 */
public class RestFilterActionPlugin extends Plugin implements ActionPlugin {

    private final Settings settings;
    private final List<RestFilter> restFilters;

    private static final Logger LOGGER = LogManager.getLogger(RestFilterActionPlugin.class);

    @Inject
    public RestFilterActionPlugin(Settings settings) {
        this.settings = settings;
        this.restFilters = SpiUtils.loadClassToInstance(RestFilter.class)
                .stream()
                .sorted(Comparator.comparing(item -> -item.ordered()))
                .collect(Collectors.toList());
    }


    @Override
    public List<Setting<?>> getSettings() {
        Set<Setting<?>> setSettings = new HashSet<>();
        for (RestFilter restFilter : this.restFilters) {
            LOGGER.info("initialize filter environment,filter name  [{}],class [{}],order [{}]", restFilter.getName(), restFilter.getClass(), restFilter.ordered());
            restFilter.initSettingsDefinition(setSettings);
        }
        return new ArrayList<>(setSettings);
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
        DocumentRestHandler documentRestHandler = new DocumentRestHandler();
        for (RestFilter restFilter : this.restFilters) {
            LOGGER.info("initialize filter [{}],class [{}],order [{}]", restFilter.getName(), restFilter.getClass(), restFilter.ordered());
            restFilter.init(settings, documentRestHandler, restController, clusterSettings, indexScopedSettings, settingsFilter, indexNameExpressionResolver, nodesInCluster);
        }
        return Collections.singletonList(documentRestHandler);
    }

    @Override
    public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
        return restHandler -> new DispatchRestFilterHandler(restHandler, threadContext, this.settings, this.restFilters);
    }
}
