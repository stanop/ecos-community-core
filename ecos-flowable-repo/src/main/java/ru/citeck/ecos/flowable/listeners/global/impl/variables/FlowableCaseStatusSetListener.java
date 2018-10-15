package ru.citeck.ecos.flowable.listeners.global.impl.variables;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.CaseStatusService;

/**
 * This class is flowable task/execution listener, which fills case status, case status names before to execution variable.
 *
 * @author Roman Makarskiy
 */
public class FlowableCaseStatusSetListener extends AbstractFlowableSaveToExecutionListener {

    private static final Log logger = LogFactory.getLog(FlowableCaseStatusSetListener.class);

    private static final String VAR_KEY_CASE_STATUS = "case_status";
    private static final String VAR_KEY_CASE_STATUS_BEFORE = "case_status_before";

    @Autowired
    private CaseStatusService caseStatusService;

    @Override
    public boolean saveIsRequired(NodeRef document) {
        return document != null && nodeService.exists(document);
    }

    @Override
    public void saveToExecution(String executionId, NodeRef document) {
        final String statusName = caseStatusService.getStatus(document);
        final String statusBeforeName = caseStatusService.getStatusBefore(document);

        if (logger.isDebugEnabled()) {
            logger.debug("Set case status name variable: <" + VAR_KEY_CASE_STATUS + "> value: <" + statusName + ">");
            logger.debug("Set case status before name variable: <" + VAR_KEY_CASE_STATUS_BEFORE + "> value: <"
                    + statusBeforeName + ">");
        }

        setVariable(executionId, VAR_KEY_CASE_STATUS, statusName);
        setVariable(executionId, VAR_KEY_CASE_STATUS_BEFORE, statusBeforeName);
    }
}
