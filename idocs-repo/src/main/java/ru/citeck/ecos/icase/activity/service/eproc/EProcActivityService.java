package ru.citeck.ecos.icase.activity.service.eproc;

import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.records2.RecordRef;

public interface EProcActivityService {

    ProcessDefinition getFullDefinition(RecordRef caseRef);

    ProcessInstance createDefaultState(RecordRef caseRef);

    ProcessInstance getFullState(RecordRef caseRef);

    void saveState(ProcessInstance processInstance);

    ActivityInstance getStateInstance(ActivityRef activityRef);

}
