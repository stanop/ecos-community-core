package ru.citeck.ecos.graphql.journal.record;

import graphql.annotations.annotationTypes.GraphQLField;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;

import java.util.List;

public class JournalRecordsConnection {

    private List<JournalRecordGql> records;
    private JournalGqlPageInfo pageInfo;
    private long totalCount;

    @GraphQLField
    public List<JournalRecordGql> records() {
        return records;
    }

    public void setRecords(List<JournalRecordGql> records) {
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
    public JournalGqlPageInfo pageInfo() {
        return pageInfo;
    }

    public void setPageInfo(JournalGqlPageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
