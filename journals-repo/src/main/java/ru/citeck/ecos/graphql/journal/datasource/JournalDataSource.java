package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;

public interface JournalDataSource {

    JournalDataSourceResult getRecords(GqlContext context, String query, String language);

}
