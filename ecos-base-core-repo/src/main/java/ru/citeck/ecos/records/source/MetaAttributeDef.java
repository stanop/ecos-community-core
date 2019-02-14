package ru.citeck.ecos.records.source;

public interface MetaAttributeDef {

    String getName();

    String getTitle();

    Class<?> getJavaClass();

    boolean isMultiple();
}
