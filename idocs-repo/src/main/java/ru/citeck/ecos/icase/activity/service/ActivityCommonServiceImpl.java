package ru.citeck.ecos.icase.activity.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;

@Service("activityCommonService")
public class ActivityCommonServiceImpl implements ActivityCommonService {

    @Autowired
    private NodeService nodeService;

    @Override
    public boolean isRoot(NodeRef caseRef) {
        return nodeService.hasAspect(caseRef, ICaseModel.ASPECT_CASE);
    }

    @Override
    public boolean isRoot(RecordRef caseRef) {
        return isRoot(RecordsUtils.toNodeRef(caseRef));
    }

    @Override
    public CaseServiceType getCaseType(NodeRef caseRef) {
        if (hasStrProp(caseRef, EcosProcessModel.PROP_PROCESS_ID) && hasStrProp(caseRef, EcosProcessModel.PROP_STATE_ID)) {
            return CaseServiceType.EPROC;
        } else {
            return CaseServiceType.ALFRESCO;
        }
    }

    private boolean hasStrProp(NodeRef caseRef, QName propQName) {
        String value = (String) nodeService.getProperty(caseRef, propQName);
        return StringUtils.isNotBlank(value);
    }

    @Override
    public CaseServiceType getCaseType(RecordRef caseRef) {
        return getCaseType(RecordsUtils.toNodeRef(caseRef));
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
