package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;

import java.util.List;

@Data
public class SentryTriggerDefinition {
    public static final String TYPE_NAME = "SENTRY";

    private List<SentryDefinition> sentries;
}
