package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface RecordsService {

    RecordsResult getRecords(RecordsQuery query);

    RecordsResult getRecords(String source, RecordsQuery query);

    Iterable<RecordRef> getIterableRecords(RecordsQuery query);

    Iterable<RecordRef> getIterableRecords(String sourceId, RecordsQuery query);

    <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records, Class<T> dataClass);

    Map<RecordRef, JsonNode> getMeta(Collection<RecordRef> records, String query);

    Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef);

    ActionResults<RecordRef> executeAction(Collection<RecordRef> records, GroupActionConfig processConfig);

    void register(RecordsDAO recordsSource);
}
