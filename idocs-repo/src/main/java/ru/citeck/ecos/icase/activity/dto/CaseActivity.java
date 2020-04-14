package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;

@Data
public class CaseActivity {
    private ActivityRef activityRef;
    private int index;
    private String title;
    private boolean active;
    private boolean repeatable;
    private ActivityState state;
}
