package ru.citeck.ecos.icase.activity.service;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.records2.RecordRef;

public interface ActivityCommonService {

    boolean isRoot(NodeRef caseRef);

    boolean isRoot(RecordRef caseRef);

    CaseServiceType getCaseType(NodeRef caseRef);

    CaseServiceType getCaseType(RecordRef caseRef);

    ActivityRef composeRootActivityRef(NodeRef caseRef);

    ActivityRef composeRootActivityRef(RecordRef caseRef);

    ActivityRef composeActivityRef(String rawActivityRef);

    EventRef composeEventRef(String rawEventRef);

}
