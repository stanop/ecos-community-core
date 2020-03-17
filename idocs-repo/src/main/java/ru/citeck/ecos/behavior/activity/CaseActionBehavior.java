package ru.citeck.ecos.behavior.activity;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.ActionDAO;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.icase.activity.service.alfresco.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import javax.annotation.PostConstruct;

/**
 * @author Pavel Simonov
 */
@Component
@DependsOn("idocs.dictionaryBootstrap")
public class CaseActionBehavior implements CaseActivityPolicies.BeforeCaseActivityStartedPolicy {

    private CaseActivityService caseActivityService;
    private AlfActivityUtils alfActivityUtils;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private ActionService actionService;
    private ActionDAO actionDAO;

    @Autowired
    public CaseActionBehavior(ServiceRegistry serviceRegistry) {
        this.caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.nodeService = serviceRegistry.getNodeService();
        this.actionService = serviceRegistry.getActionService();
        this.actionDAO = (ActionDAO) serviceRegistry.getService(CiteckServices.ACTION_DAO);
    }

    @PostConstruct
    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.BeforeCaseActivityStartedPolicy.QNAME,
                ActionModel.TYPE_ACTION,
                new ChainingJavaBehaviour(this, "beforeCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
    }

    @Override
    public void beforeCaseActivityStarted(NodeRef actionRef) {
        if (!nodeService.exists(actionRef)) {
            return;
        }

        Action action = actionDAO.readAction(actionRef);

        ActivityRef actionActivityRef = alfActivityUtils.composeActivityRef(actionRef);

        RecordRef documentRef = actionActivityRef.getProcessId();
        NodeRef documentNodeRef = RecordsUtils.toNodeRef(documentRef);
        actionService.executeAction(action, documentNodeRef);

        caseActivityService.stopActivity(actionActivityRef);
    }
}
