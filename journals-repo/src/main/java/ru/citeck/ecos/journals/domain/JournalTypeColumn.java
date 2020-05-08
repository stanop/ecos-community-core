package ru.citeck.ecos.journals.domain;

import lombok.Data;

import java.util.Map;

@Data
public class JournalTypeColumn {

    private String text;
    private String type;
    private String editorKey;
    private String javaClass;
    private String attribute;
    private String schema;
    private JournalTypeColumnFormatter formatter;
    private Map<String, String> params;
    private boolean isDefault;
    private boolean isSearchable;
    private boolean isSortable;
    private boolean isVisible;
    private boolean isGroupable;
}
