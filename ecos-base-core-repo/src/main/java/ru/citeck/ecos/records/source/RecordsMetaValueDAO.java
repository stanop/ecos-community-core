package ru.citeck.ecos.records.source;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;

import java.util.List;

/**
 * @deprecated
 */
public interface RecordsMetaValueDAO {

    List<MetaValue> getMetaValues(GqlContext context, List<RecordRef> recordRefs);
}
