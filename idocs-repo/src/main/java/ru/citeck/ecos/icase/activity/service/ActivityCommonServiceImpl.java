package ru.citeck.ecos.icase.activity.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.records2.RecordRef;

@Service("activityCommonService")
public class ActivityCommonServiceImpl implements ActivityCommonService {

    @Override
    public CaseServiceType getCaseType(NodeRef caseRef) {
        return CaseServiceType.ALFRESCO;
    }

    @Override
    public CaseServiceType getCaseType(RecordRef caseRef) {
        return CaseServiceType.ALFRESCO;
    }

    @Override
    public ActivityRef composeRootActivityRef(NodeRef caseRef) {
        RecordRef recordRef = RecordRef.valueOf(caseRef.toString());
        return composeRootActivityRef(recordRef);
    }

    @Override
    public ActivityRef composeRootActivityRef(RecordRef caseRef) {
        CaseServiceType type = getCaseType(caseRef);
        return ActivityRef.of(type, caseRef, ActivityRef.ROOT_ID);
    }
}
