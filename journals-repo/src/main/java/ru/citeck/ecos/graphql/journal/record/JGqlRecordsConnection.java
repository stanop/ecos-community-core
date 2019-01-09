package ru.citeck.ecos.graphql.journal.record;

import ru.citeck.ecos.graphql.journal.JGqlPageInfo;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.Collections;
import java.util.List;

public class JGqlRecordsConnection {

    private List<MetaValue> records = Collections.emptyList();
    private JGqlPageInfo pageInfo = new JGqlPageInfo();
    private long totalCount = 0;

    public List<MetaValue> records() {
        return records;
    }

    public void setRecords(List<MetaValue> records) {
        this.records = records;
    }

    public long totalCount() {
        return totalCount == 0 ? (records != null ? records.size() : 0) : totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public JGqlPageInfo pageInfo() {
        return pageInfo;
    }

    public void setPageInfo(JGqlPageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
