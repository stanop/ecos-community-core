package ru.citeck.ecos.journals;

import lombok.Data;

import java.util.Map;

@Data
public class JournalFormatter {
    private String name;
    private Map<String, String> params;
}
