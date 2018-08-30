package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

    <T> Map<RecordRef, T> getMeta(Collection<RecordRef> records, Class<T> dataClass);

    Map<RecordRef, ObjectNode> getMeta(Collection<RecordRef> records, String query);

    Optional<MetaValue> getMetaValue(GqlContext context, String source, String id);

    Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef);

    /* List<ActionResult<String>> executeAction(Collection<RecordRef> records,
                                              String actionId,
                                              GroupActionConfig config);

     List<ActionResult<String>> executeAction(String sourceId,
                                              RecordsQuery query,
                                              String actionId,
                                              GroupActionConfig config);

     List<ActionResult<String>> executeAction(String sourceId,
                                              RecordsQuery query,
                                              String actionId,
                                              GroupActionConfig config);*/
    Optional<AttributeInfo> getAttributeInfo(String source, String name);

    Optional<AttributeInfo> getAttributeInfo(RecordRef recordRef);

    void register(RecordsDAO recordsSource);
}
