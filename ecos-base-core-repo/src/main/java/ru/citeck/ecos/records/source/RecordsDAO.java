package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecordsDAO {

    RecordsResult queryRecords(RecordsQuery query);

    Map<RecordRef, JsonNode> queryMeta(Collection<RecordRef> records, String gqlSchema);

    <V> Map<RecordRef, V> queryMeta(Collection<RecordRef> records, Class<V> metaClass);

    Optional<MetaValue> getMetaValue(GqlContext context, String id);

    ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config);

    String getId();
}
