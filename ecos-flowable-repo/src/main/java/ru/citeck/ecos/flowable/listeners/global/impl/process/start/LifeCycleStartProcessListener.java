package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.lifecycle.LifeCycleService;

/**
 * Lifecycle start process listener
 */
public class LifeCycleStartProcessListener implements GlobalStartExecutionListener {

    private LifeCycleService lifeCycleService;
    private NodeService nodeService;

    /**
     * Notify
     *
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        NodeRef docRef = FlowableListenerUtils.getDocument(delegateExecution, nodeService);
        if (docRef == null) {
            return;
        }
        String definitionId = FlowableConstants.ENGINE_PREFIX + delegateExecution.getProcessDefinitionId();
        lifeCycleService.doTransitionOnStartProcess(docRef, definitionId, delegateExecution.getVariables());
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
}
