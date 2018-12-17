package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * Package fields filling listener
 */
public class PackageFieldsFillingListener implements GlobalStartExecutionListener {

    private NodeService nodeService;

    /**
     * Notify
     *
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        NodeRef packageRef = FlowableListenerUtils.getWorkflowPackage(delegateExecution);
        if (packageRef == null) {
            return;
        }

        Map<QName, Serializable> props = nodeService.getProperties(packageRef);
        boolean propsChanged = false;
        if (!props.containsKey(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID)) {
            props.put(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, FlowableConstants.ENGINE_PREFIX + delegateExecution
                    .getProcessInstanceId());
            propsChanged = true;
        }
        if (!props.containsKey(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME)) {
            props.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, FlowableConstants.ENGINE_PREFIX + delegateExecution
                    .getProcessDefinitionId().split(":")[0]);
            propsChanged = true;
        }
        if (propsChanged) {
            nodeService.setProperties(packageRef, props);
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
