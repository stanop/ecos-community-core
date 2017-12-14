package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.lifecycle.LifeCycleService;

/**
 * Lifecycle start process listener
 */
public class LifeCycleStartProcessListener implements GlobalStartExecutionListener {

    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_PREFIX = "flowable$";

    /**
     * Lifecycle service
     */
    private LifeCycleService lifeCycleService;

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
        if(docRef == null) {
            return;
        }
        String definitionId = FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessDefinitionId();
        lifeCycleService.doTransitionOnStartProcess(docRef, definitionId, delegateExecution.getVariables());
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set lifecycle service
     * @param lifeCycleService Lifecycle service
     */
    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
}
