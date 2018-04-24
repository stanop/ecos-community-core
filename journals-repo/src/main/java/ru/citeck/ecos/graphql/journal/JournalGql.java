package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.journals.JournalType;

public class JournalGql {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

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
    public JournalRecordsConnection recordsConnection(@GraphQLName("after") String after,
                                                      @GraphQLName("first") Integer first,
                                                      @GraphQLName("q") String query,
                                                      @GraphQLName("lang") String language) {
        if (recordsConnection == null) {
            if (first == null) {
                first = DEFAULT_PAGE_SIZE;
            }
            recordsConnection = dataSource.getRecords(context, query, language, after, first);
        }

        return recordsConnection;
    }

}
