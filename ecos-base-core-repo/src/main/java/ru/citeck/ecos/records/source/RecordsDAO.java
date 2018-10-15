package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecordsDAO {

    RecordsResult queryRecords(RecordsQuery query);

    List<ObjectNode> getMeta(Collection<RecordRef> records, String gqlSchema);

    Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef);

    ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config);

    String getId();
}
