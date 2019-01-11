package ru.citeck.ecos.records.meta;

import ru.citeck.ecos.records.RecordMeta;

import java.util.List;

public interface RecordsMetaService {

    List<AttributeSchema> getAttributes(Class<?> metaClass);

    RecordMeta createMeta(List<AttributeSchema> schema, )

    <T> T createMeta(Class<?> metaClass, RecordMeta meta);

}
