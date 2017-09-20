package ru.citeck.ecos.behavior.activity;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.ActionDAO;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActionModel;

/**
 * @author Pavel Simonov
 */
public class CaseActionBehavior implements CaseActivityPolicies.BeforeCaseActivityStartedPolicy {

    private static final Log log = LogFactory.getLog(CaseActionBehavior.class);

    private CaseActivityService caseActivityService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private ActionService actionService;
    private ActionDAO actionDAO;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.BeforeCaseActivityStartedPolicy.QNAME,
                ActionModel.TYPE_ACTION,
                new ChainingJavaBehaviour(this, "beforeCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
    }

    @Override
    public void beforeCaseActivityStarted(NodeRef actionRef) {
        if (!nodeService.exists(actionRef)) return;

        Action action = actionDAO.readAction(actionRef);
        actionService.executeAction(action, caseActivityService.getDocument(actionRef));

        caseActivityService.stopActivity(actionRef);
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        this.actionDAO = actionDAO;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }
}