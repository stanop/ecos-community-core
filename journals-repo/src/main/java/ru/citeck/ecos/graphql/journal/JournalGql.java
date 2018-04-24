package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSourceResult;
import ru.citeck.ecos.journals.JournalType;


public class JournalGql {

    private JournalDataSourceResult dataSourceResult;

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
    public JournalDataSourceResult records(@GraphQLName("q") String query,
                                           @GraphQLName("lang") String language) {
        if (dataSourceResult == null) {
            dataSourceResult = dataSource.getRecords(context, query, language);
        }
        return dataSourceResult;
    }

}
