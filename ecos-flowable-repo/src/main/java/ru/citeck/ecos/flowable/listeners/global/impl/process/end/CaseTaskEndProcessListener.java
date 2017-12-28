package ru.citeck.ecos.flowable.listeners.global.impl.process.end;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import java.util.List;

/**
 * Case task end process listener
 */
public class CaseTaskEndProcessListener implements GlobalEndExecutionListener {

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Case activity service
     */
    private CaseActivityService caseActivityService;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                if (FlowableListenerUtils.getDocument(delegateExecution, nodeService) == null) {
                    return null;
                }
                stopActivity(delegateExecution);
                return null;
            }
        });
    }

    /**
     * Stop activity
     * @param delegateExecution Execution
     */
    private void stopActivity(DelegateExecution delegateExecution) {

        NodeRef bpmPackage = FlowableListenerUtils.getWorkflowPackage(delegateExecution);
        nodeService.setProperty(bpmPackage, CiteckWorkflowModel.PROP_IS_WORKFLOW_ACTIVE, false);
        List<AssociationRef> packageAssocs = nodeService.getSourceAssocs(bpmPackage, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);

        if(packageAssocs != null && packageAssocs.size() > 0) {
            ActionConditionUtils.getProcessVariables().putAll(delegateExecution.getVariables());
            caseActivityService.stopActivity(packageAssocs.get(0).getSourceRef());
        }
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set case activity service
     * @param caseActivityService Case activity service
     */
    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }
}
