package com.lhstack.opensearch.handler;

import org.opensearch.client.node.NodeClient;
import org.opensearch.common.Table;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.action.cat.AbstractCatAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/17 9:28
 * @Modify by
 */
public class DocumentRestHandler extends AbstractCatAction {

    private static final Table TABLE = new Table();

    private final Set<String> documents = new HashSet<>();

    @Override
    protected RestChannelConsumer doCatRequest(RestRequest request, NodeClient client) {
        return null;
    }

    @Override
    protected void documentation(StringBuilder sb) {
        this.documents.forEach(item -> sb.append(String.format("%s\r\n", item)));
    }

    public void addDocument(String document) {
        this.documents.add(document);
    }

    public void addDocuments(String... documents) {
        this.documents.addAll(Arrays.asList(documents));
    }

    public void addDocuments(List<String> documents) {
        this.documents.addAll(documents);
    }

    @Override
    protected Table getTableWithHeader(RestRequest request) {
        return TABLE;
    }

    @Override
    public String getName() {
        return "rest-filter-api-document";
    }
}
