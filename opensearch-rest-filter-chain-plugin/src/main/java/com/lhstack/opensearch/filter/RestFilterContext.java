package com.lhstack.opensearch.filter;

import org.opensearch.client.node.NodeClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/18 17:42
 * @Modify By
 */
public class RestFilterContext {

    private final Map<String, Object> attributes;

    private final ThreadContext threadContext;

    private final NodeClient nodeClient;

    private final Settings settings;

    public RestFilterContext(ThreadContext threadContext, NodeClient nodeClient, Settings settings) {
        this.threadContext = threadContext;
        this.nodeClient = nodeClient;
        this.settings = settings;
        this.attributes = new HashMap<>(1);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public ThreadContext getThreadContext() {
        return threadContext;
    }

    public NodeClient getNodeClient() {
        return nodeClient;
    }

    public Settings getSettings() {
        return settings;
    }

    public <T> T getAttribute(String key) {
        return (T) this.attributes.get(key);
    }

    public <T> T getAttribute(String key, T defaultValue) {
        T attribute;
        return Objects.nonNull(attribute = this.getAttribute(key)) ? attribute : defaultValue;
    }

    public RestFilterContext setAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public RestFilterContext addResponseHeader(String name,String value){
        this.threadContext.addResponseHeader(name,value);
        return this;
    }

    public RestFilterContext addResponseHeader(String name, String value, Function<String,String> unique){
        this.threadContext.addResponseHeader(name,value,unique);
        return this;
    }

}
