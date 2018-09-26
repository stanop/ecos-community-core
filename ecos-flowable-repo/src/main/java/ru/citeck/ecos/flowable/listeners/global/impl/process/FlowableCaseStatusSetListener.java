package ru.citeck.ecos.flowable.listeners.global.impl.process;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalTakeExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.icase.CaseStatusService;

/**
 * This class is flowable execution listener, which fills case status, case status names before to execution variable.
 *
 * @author Roman Makarskiy
 */
public class FlowableCaseStatusSetListener implements GlobalStartExecutionListener, GlobalEndExecutionListener,
        GlobalTakeExecutionListener {

    private static final Log logger = LogFactory.getLog(FlowableCaseStatusSetListener.class);

    private static final String VAR_KEY_CASE_STATUS = "case_status";
    private static final String VAR_KEY_CASE_STATUS_BEFORE = "case_status_before";

    @Autowired
    private NodeService nodeService;
    @Autowired
    private CaseStatusService caseStatusService;

    @Override
    public void notify(DelegateExecution execution) {
        NodeRef document = FlowableListenerUtils.getDocument(execution, nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        final String statusName = caseStatusService.getStatus(document);
        final String statusBeforeName = caseStatusService.getStatusBefore(document);

        if (logger.isDebugEnabled()) {
            logger.debug("Set case status name variable: <" + VAR_KEY_CASE_STATUS + "> value: <" + statusName + ">");
            logger.debug("Set case status before name variable: <" + VAR_KEY_CASE_STATUS_BEFORE + "> value: <"
                    + statusBeforeName + ">");
        }

        execution.setVariable(VAR_KEY_CASE_STATUS, statusName);
        execution.setVariable(VAR_KEY_CASE_STATUS_BEFORE, statusBeforeName);
    }
}
