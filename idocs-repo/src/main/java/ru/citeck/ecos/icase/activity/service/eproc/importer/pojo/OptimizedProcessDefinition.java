package ru.citeck.ecos.icase.activity.service.eproc.importer.pojo;

import lombok.Data;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.icase.activity.dto.ActivityDefinition;
import ru.citeck.ecos.icase.activity.dto.ProcessDefinition;
import ru.citeck.ecos.icase.activity.dto.SentryDefinition;

import java.util.List;
import java.util.Map;

@Data
public class OptimizedProcessDefinition {
    private Definitions xmlProcessDefinition;
    private ProcessDefinition processDefinition;
    private Map<String, ActivityDefinition> idToActivityCache;
    private Map<String, SentryDefinition> idToSentryCache;
    private Map<SentrySearchKey, List<SentryDefinition>> sentrySearchCache;
}
