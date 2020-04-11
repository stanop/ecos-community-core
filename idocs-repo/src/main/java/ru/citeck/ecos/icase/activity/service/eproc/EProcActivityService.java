package ru.citeck.ecos.icase.activity.service.eproc;

import org.alfresco.util.Pair;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.ProcessDefinition;
import ru.citeck.ecos.icase.activity.dto.ProcessInstance;
import ru.citeck.ecos.records2.RecordRef;

public interface EProcActivityService {

    Pair<String, byte[]> getRawDefinitionForType(RecordRef caseRef);

    ProcessDefinition getFullDefinition(RecordRef caseRef);

    ProcessInstance createDefaultState(RecordRef caseRef);

    ProcessInstance createDefaultState(RecordRef caseRef, String revisionId, ProcessDefinition definition);

    ProcessInstance getFullState(RecordRef caseRef);

    void saveState(ProcessInstance processInstance);

    ActivityInstance getStateInstance(ActivityRef activityRef);

}
