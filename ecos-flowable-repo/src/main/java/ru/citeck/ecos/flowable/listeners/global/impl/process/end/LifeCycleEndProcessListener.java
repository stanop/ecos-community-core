package ru.citeck.ecos.flowable.listeners.global.impl.process.end;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.lifecycle.LifeCycleService;

/**
 * Lifecycle end process listener
 */
public class LifeCycleEndProcessListener implements GlobalEndExecutionListener {

    private LifeCycleService lifeCycleService;
    private NodeService nodeService;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        AuthenticationUtil.runAsSystem(() -> {
            NodeRef docRef = FlowableListenerUtils.getDocument(delegateExecution, nodeService);
            if(docRef == null) {
                return null;
            }
            String definitionId = FlowableConstants.ENGINE_PREFIX + delegateExecution.getProcessDefinitionId();
            lifeCycleService.doTransitionOnEndProcess(docRef, definitionId, delegateExecution.getVariables());
            return null;
        });
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
}
