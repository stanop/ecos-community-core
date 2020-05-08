package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProcessDefinition {
    private String id;
    private ActivityDefinition activityDefinition;
    private List<RoleReference> roles;
}
