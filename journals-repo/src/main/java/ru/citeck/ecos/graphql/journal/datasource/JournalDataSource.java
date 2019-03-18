package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;

public interface JournalDataSource {

    JGqlRecordsConnection getRecords(AlfGqlContext context,
                                     String query,
                                     String language,
                                     JGqlPageInfoInput pageInfo);
}
