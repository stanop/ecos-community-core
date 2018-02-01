package ru.citeck.ecos.job.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.service.EcosCoreServices;

/**
 * @author Pavel Simonov
 */
public class ChangeStatusByDateWork extends ExecuteActionByDateWork {

    private static final Log logger = LogFactory.getLog(ChangeStatusByDateWork.class);

    private CaseStatusService caseStatusService;

    @Override
    public void init() {
        super.init();
        this.caseStatusService = EcosCoreServices.getCaseStatusService(serviceRegistry);
    }

    @Override
    protected boolean checkStatus(NodeRef nodeRef) {
        String status = caseStatusService.getStatus(nodeRef);
        return !actionKey.equals(status) && super.checkStatus(nodeRef);
    }

    @Override
    public void process(NodeRef entry) {
        if (logger.isDebugEnabled()) {
            logger.debug("Set status <" + actionKey + "> on case: " + entry);
        }
        caseStatusService.setStatus(entry, actionKey);
    }
}
