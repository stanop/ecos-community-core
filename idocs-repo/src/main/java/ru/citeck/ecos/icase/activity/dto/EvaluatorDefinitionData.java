package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;

import java.util.Map;

@Data
public class EvaluatorDefinitionData {
    private String type;
    private Map<String, String> attributes;
}
