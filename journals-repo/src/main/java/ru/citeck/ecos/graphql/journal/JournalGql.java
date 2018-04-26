package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.journals.JournalType;

public class JournalGql {

    private JournalRecordsConnection recordsConnection;

    private GqlContext context;

    private JournalType journalType;
    private JournalDataSource dataSource;

    public JournalGql(JournalType journalType, GqlContext context, JournalDataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
        this.journalType = journalType;
    }

    @GraphQLField
    public String id() {
        return journalType.getId();
    }

    @GraphQLField
    public JournalRecordsConnection recordsConnection(@GraphQLName("pageInfo") JournalGqlPageInfoInput pageInfo,
                                                      @GraphQLName("q") String query,
                                                      @GraphQLName("lang") String language) {
        if (recordsConnection == null) {
            recordsConnection = dataSource.getRecords(context, query, language, pageInfo);
        }

        return recordsConnection;
    }

}
