package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.journals.records.JournalRecordsResult;
import ru.citeck.ecos.records.RecordRef;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface JournalDataSource {

    JGqlRecordsConnection getRecords(GqlContext context,
                                     String query,
                                     String language,
                                     JGqlPageInfoInput pageInfo);

    String getServerId();

    JournalRecordsResult queryIds(GqlContext context,
                                  String query,
                                  String language,
                                  JGqlPageInfoInput pageInfo);

    List<MetaValue> convertToGqlValue(GqlContext context,
                                      List<RecordRef> remoteRefList);

    JournalData queryMetadata(String gqlQuery,
                              String dataSourceBeanName,
                              JournalRecordsResult recordsResult);

    default boolean isSupportsSplitLoading() {
        return false;
    }

    Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName);

    default List<String> getDefaultAttributes() {
        return Collections.emptyList();
    }
}
