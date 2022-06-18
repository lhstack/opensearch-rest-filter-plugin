package com.lhstack.opensearch.filter;

import com.lhstack.opensearch.handler.DocumentRestHandler;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.common.settings.*;
import org.opensearch.rest.RestChannel;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestRequest;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 9:51
 * @Modify by
 */
public interface RestFilter {

    /**
     * 责任链模式
     *
     * @param request     当前request
     * @param restChannel 当前连接
     * @param context     当前请求的上下文，包含属性设置，settings配置获取，添加header
     * @param filterChain 过滤链
     * @throws Exception
     */
    void filter(RestRequest request, RestChannel restChannel, RestFilterContext context, RestFilterChain filterChain) throws Exception;

    /**
     * 排序
     *
     * @return
     */
    default int ordered() {
        return 0;
    }

    /**
     * filter名称
     *
     * @return
     */
    default String getName() {
        return this.getClass().getCanonicalName();
    }

    default void init(Settings settings, DocumentRestHandler documentRestHandler, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {

    }

    /**
     * 初始化环境，向opensearch.yml注册配置
     *
     * @param settings
     */
    default void initSettingsDefinition(Set<Setting<?>> settings) {

    }
}
