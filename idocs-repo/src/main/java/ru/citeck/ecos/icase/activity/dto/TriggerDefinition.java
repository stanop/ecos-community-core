package ru.citeck.ecos.icase.activity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.citeck.ecos.commons.data.ObjectData;

@Data
@ToString(exclude = {"parentActivityTransitionDefinition"})
@EqualsAndHashCode(exclude = {"parentActivityTransitionDefinition"})
@JsonIgnoreProperties(ignoreUnknown = true, value = {"parentActivityTransitionDefinition"})
@ecos.com.fasterxml.jackson210.annotation.JsonIgnoreProperties(ignoreUnknown = true, value = {"parentActivityTransitionDefinition"})
@ecos.com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true, value = {"parentActivityTransitionDefinition"})
public class TriggerDefinition {
    private String id;
    private String type;
    private ObjectData data;

    private ActivityTransitionDefinition parentActivityTransitionDefinition;
}
