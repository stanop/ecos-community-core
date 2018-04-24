package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;

public interface JournalDataSource {

    JournalRecordsConnection getRecords(GqlContext context, String query, String language, String after, Integer first);

    JournalAttributeInfo getAttributeInfo(String attributeName);

}
