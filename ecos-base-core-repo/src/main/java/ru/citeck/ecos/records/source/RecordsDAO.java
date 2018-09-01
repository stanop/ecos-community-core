package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface RecordsDAO {

    DaoRecordsResult queryRecords(RecordsQuery query);

    Map<String, JsonNode> queryMeta(Collection<String> records, String gqlSchema);

    <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass);

    Optional<AttributeInfo> getAttributeInfo(String name);

    Optional<MetaValue> getMetaValue(GqlContext context, String id);

    String getId();
}
