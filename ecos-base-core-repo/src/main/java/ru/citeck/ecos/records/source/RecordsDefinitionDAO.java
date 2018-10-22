package ru.citeck.ecos.records.source;

import java.util.Collection;
import java.util.List;

public interface RecordsDefinitionDAO extends RecordsDAO {

    List<MetaValueTypeDef> getTypesDefinition(Collection<String> names);

    List<MetaAttributeDef> getAttsDefinition(Collection<String> names);
}
