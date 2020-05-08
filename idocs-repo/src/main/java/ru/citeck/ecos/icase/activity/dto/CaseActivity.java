package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CaseActivity {
    private ActivityRef activityRef;
    private int index;
    private ActivityType activityType;
    private String title;
    private boolean active;
    private boolean repeatable;
    private ActivityState state;
    private Date startDate;
    private Date completeDate;
}
