package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.model.CiteckWorkflowModel;

/**
 * Set priority workflow listener
 */
public class SetPriorityWorkflowListener implements GlobalStartExecutionListener {

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        NodeRef docRef = FlowableListenerUtils.getDocument(delegateExecution, nodeService);
        if (docRef == null) {
            return;
        }
        Object bpmPriorityObj = delegateExecution.getVariable("bpm_workflowPriority");
        Integer bpmPriority = null;
        if (bpmPriorityObj instanceof Integer) {
            bpmPriority = (Integer) bpmPriorityObj;
        }
        if (bpmPriority == null) {
            return;
        }
        Object docPriorityObj = nodeService.getProperty(docRef, CiteckWorkflowModel.PROP_PRIORITY);
        Integer docPriority = null;
        if (docPriorityObj instanceof Integer) {
            docPriority = (Integer) docPriorityObj;
        }
        if (!bpmPriority.equals(docPriority)) {
            nodeService.setProperty(docRef, CiteckWorkflowModel.PROP_PRIORITY, bpmPriority);
        }
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
