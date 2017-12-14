package ru.citeck.ecos.flowable.listeners.global.impl.process.end;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.lifecycle.LifeCycleService;

/**
 * Lifecycle end process listener
 */
public class LifeCycleEndProcessListener implements GlobalEndExecutionListener {

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
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                NodeRef docRef = FlowableListenerUtils.getDocument(delegateExecution, nodeService);
                if(docRef == null) {
                    return null;
                }
                String definitionId = FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessDefinitionId();
                lifeCycleService.doTransitionOnEndProcess(docRef, definitionId, delegateExecution.getVariables());
                return null;
            }
        });
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
