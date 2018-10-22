package ru.citeck.ecos.records.source;

import java.util.Optional;

public interface MetaValueTypeDef {

    String getName();

    String getTitle();

    Optional<MetaAttributeDef> getAttDefinition(String name);
}
