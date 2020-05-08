package ru.citeck.ecos.journals.domain;

import lombok.Data;

@Data
public class JournalMetaAttributeInfo {

    private String type;
    private String editorKey;
    private Class<?> javaClass;
}
