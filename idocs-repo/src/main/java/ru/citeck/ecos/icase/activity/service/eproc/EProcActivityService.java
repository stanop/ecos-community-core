package ru.citeck.ecos.icase.activity.service.eproc;

import org.alfresco.util.Pair;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.records2.RecordRef;

import java.util.List;

public interface EProcActivityService {

    Pair<String, byte[]> getRawDefinitionForType(RecordRef caseRef);

    ProcessDefinition getFullDefinition(RecordRef caseRef);

    ProcessInstance createDefaultState(RecordRef caseRef);

    ProcessInstance createDefaultState(RecordRef caseRef, String revisionId, ProcessDefinition definition);

    ProcessInstance getFullState(RecordRef caseRef);

    void saveState(ProcessInstance processInstance);

    ActivityInstance getStateInstance(ActivityRef activityRef);

    SentryDefinition getSentryDefinition(EventRef eventRef);

    List<SentryDefinition> findSentriesBySourceRefAndEventType(RecordRef caseRef, String sourceRef, String eventType);

}
