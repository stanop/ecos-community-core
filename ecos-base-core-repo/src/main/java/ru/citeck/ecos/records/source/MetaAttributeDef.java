package ru.citeck.ecos.records.source;

public interface MetaAttributeDef {

    String getName();

    String getTitle();

    Class<?> getDataType();

    boolean isMultiple();
}
