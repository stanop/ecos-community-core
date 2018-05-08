package ru.citeck.ecos.workflow.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.workflow.listeners.AbstractExecutionListener;
import ru.citeck.ecos.workflow.listeners.ListenerUtils;

import java.util.Objects;

/**
 * @author Andrey Platunov on 25.04.2018
 */
public class SetConfirmationStatusToOrdersListener extends AbstractExecutionListener {

    private NodeService nodeService;
    private WorkflowService workflowService;
    private QName requiredDocType;
    private String requiredWorkflowName;
    private QName fieldToSet;
    private String statusToSet;

    @Override
    protected void notifyImpl(DelegateExecution execution) throws Exception {

        NodeRef document = ListenerUtils.getDocument(execution, nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }
        QName currentDocType = nodeService.getType(document);
        if (!Objects.equals(currentDocType, requiredDocType)) {
            return;
        }

        ExecutionEntity executionEntity = (ExecutionEntity) execution;

        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();

        if (processDefinition == null) {
            return;
        }

        String currentWorkflowName = processDefinition.getKey();

        if (StringUtils.isEmpty(requiredWorkflowName)
                || StringUtils.isEmpty(currentWorkflowName)
                || !Objects.equals(currentWorkflowName, requiredWorkflowName)) {
            return;
        }

        nodeService.setProperty(document, fieldToSet, statusToSet);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setRequiredDocType(QName requiredDocType) {
        this.requiredDocType = requiredDocType;
    }

    public void setRequiredWorkflowName(String requiredWorkflowName) {
        this.requiredWorkflowName = requiredWorkflowName;
    }

    public void setFieldToSet(QName fieldToSet) {
        this.fieldToSet = fieldToSet;
    }

    public void setStatusToSet(String statusToSet) {
        this.statusToSet = statusToSet;
    }
}
