package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.records.RecordRef;

import java.util.List;

public interface RecordsMetaDAO {

    List<ObjectNode> getMeta(List<RecordRef> records, String gqlSchema);
}
