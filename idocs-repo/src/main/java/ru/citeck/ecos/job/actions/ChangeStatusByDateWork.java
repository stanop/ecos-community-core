package ru.citeck.ecos.job.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.service.EcosCoreServices;

/**
 * @author Pavel Simonov
 */
public class ChangeStatusByDateWork extends ExecuteActionByDateWork {

    private static final Log logger = LogFactory.getLog(ChangeStatusByDateWork.class);

    private String targetStatus;

    private CaseStatusService caseStatusService;

    @Override
    public void init() {
        ParameterCheck.mandatory("targetStatus", targetStatus);
        super.init();
        this.caseStatusService = EcosCoreServices.getCaseStatusService(serviceRegistry);
    }

    @Override
    public void process(NodeRef entry) {
        if (logger.isDebugEnabled()) {
            logger.debug("Set status <" + targetStatus + "> on case: " + entry);
        }
        caseStatusService.setStatus(entry, targetStatus);
    }

    @Override
    public String toString() {
        return super.toString() + " TargetStatus: " + targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }
}
