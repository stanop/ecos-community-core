package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;
import ru.citeck.ecos.commons.data.ObjectData;

import java.util.List;

@Data
public class ActivityDefinition {
    private String id;
    private ActivityType type;
    private int index;
    private boolean repeatable;
    private List<ActivityDefinition> activities;
    private List<ActivityTransitionDefinition> transitions;
    private ObjectData data;
}
