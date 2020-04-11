package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;
import ru.citeck.ecos.commons.data.ObjectData;

@Data
public class EvaluatorDefinition {
    private String id;
    private Boolean inverse;
    private ObjectData data;
}
