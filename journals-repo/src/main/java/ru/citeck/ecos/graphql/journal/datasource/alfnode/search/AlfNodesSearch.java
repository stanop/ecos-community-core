package ru.citeck.ecos.graphql.journal.datasource.alfnode.search;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;

public interface AlfNodesSearch {

    JGqlRecordsConnection query(GqlContext context,
                                String query,
                                JGqlPageInfoInput pageInfo);

    String getLanguage();
}
