package ru.citeck.ecos.records.source;

import java.util.Collection;
import java.util.List;

public interface RecordsDefinitionDAO extends RecordsQueryDAO {

    List<MetaAttributeDef> getAttributesDef(Collection<String> names);
}
