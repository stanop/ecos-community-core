package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;
import ru.citeck.ecos.graphql.journal.JGqlPageInfo;

import java.util.Collections;
import java.util.List;

public class JGqlRecordsConnection {

    private List<JGqlAttributeValue> records = Collections.emptyList();
    private JGqlPageInfo pageInfo = new JGqlPageInfo();
    private long totalCount = 0;

    @GraphQLField
    public List<JGqlAttributeValue> records() {
        return records;
    }

    public void setRecords(List<JGqlAttributeValue> records) {
        this.records = records;
    }

    @GraphQLField
    public long totalCount() {
        return totalCount == 0 ? (records != null ? records.size() : 0) : totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    @GraphQLField
    public JGqlPageInfo pageInfo() {
        return pageInfo;
    }

    public void setPageInfo(JGqlPageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
