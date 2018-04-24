package ru.citeck.ecos.graphql.journal.datasource;

import graphql.annotations.annotationTypes.GraphQLField;
import ru.citeck.ecos.graphql.journal.JournalRecordGql;

import java.util.List;

public class JournalDataSourceResult {

    private List<JournalRecordGql> records;
    private JournalDataSourcePaging paging;

    @GraphQLField
    public List<JournalRecordGql> records() {
        return records;
    }

    public void setRecords(List<JournalRecordGql> records) {
        this.records = records;
    }

    @GraphQLField
    public JournalDataSourcePaging paging() {
        return paging;
    }

    public void setPaging(JournalDataSourcePaging paging) {
        this.paging = paging;
    }
}
