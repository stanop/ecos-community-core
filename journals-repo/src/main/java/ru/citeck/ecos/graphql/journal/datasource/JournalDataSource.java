package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface JournalDataSource {

    JGqlRecordsConnection getRecords(GqlContext context,
                                     String query,
                                     String language,
                                     JGqlPageInfoInput pageInfo);

    Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName);

    default List<String> getDefaultAttributes() {
        return Collections.emptyList();
    }
}
