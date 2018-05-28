package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface JournalDataSource {

    JournalRecordsConnection getRecords(GqlContext context, String query, String language, JournalGqlPageInfoInput pageInfo);

    Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName);

    default List<String> getDefaultAttributes() {
        return Collections.emptyList();
    }
}
