package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.records.RecordRef;

import java.util.Collection;
import java.util.List;

public interface RecordsMetaDAO {

    List<ObjectNode> getMeta(Collection<RecordRef> records, String gqlSchema);
}
