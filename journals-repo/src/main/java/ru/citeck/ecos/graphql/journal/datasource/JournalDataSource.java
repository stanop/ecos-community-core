package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.journals.JournalType;

import java.util.Optional;

public interface JournalDataSource {

    JournalRecordsConnection getRecords(GqlContext context, String query, String language, String after, Integer first);

    Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName);

}
