package ru.citeck.ecos.records.type;

import ru.citeck.ecos.records2.RecordRef;

public interface TypeInfoProvider {

    TypeDto getType(RecordRef typeRef);
}
