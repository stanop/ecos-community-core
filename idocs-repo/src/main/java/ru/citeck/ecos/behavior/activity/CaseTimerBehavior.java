package ru.citeck.ecos.behavior.activity;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.icase.timer.CaseTimerService;
import ru.citeck.ecos.model.CaseTimerModel;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseTimerBehavior implements CaseActivityPolicies.BeforeCaseActivityStartedPolicy,
                                          CaseActivityPolicies.OnCaseActivityStartedPolicy,
                                          CaseActivityPolicies.OnCaseActivityResetPolicy,
                                          CaseActivityPolicies.OnCaseActivityStoppedPolicy,
                                          NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          NodeServicePolicies.OnCreateNodePolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private CaseTimerService caseTimerService;
    private CaseActivityService caseActivityService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.BeforeCaseActivityStartedPolicy.QNAME,
                CaseTimerModel.TYPE_TIMER,
                new ChainingJavaBehaviour(this, "beforeCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.OnCaseActivityStartedPolicy.QNAME,
                CaseTimerModel.TYPE_TIMER,
                new ChainingJavaBehaviour(this, "onCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.OnCaseActivityStoppedPolicy.QNAME,
                CaseTimerModel.TYPE_TIMER,
                new ChainingJavaBehaviour(this, "onCaseActivityStopped", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.OnCaseActivityResetPolicy.QNAME,
                CaseTimerModel.TYPE_TIMER,
                new ChainingJavaBehaviour(this, "onCaseActivityReset", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                CaseTimerModel.TYPE_TIMER,
                new ChainingJavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                CaseTimerModel.TYPE_TIMER,
                new ChainingJavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    /* validation */

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        checkTimerData(childAssocRef.getChildRef());
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!Objects.equals(before.get(CaseTimerModel.PROP_TIMER_EXPRESSION),
                            after.get(CaseTimerModel.PROP_TIMER_EXPRESSION))) {
            checkTimerData(nodeRef);
        }
    }

    private void checkTimerData(final NodeRef timerRef) {
        AuthenticationUtil.runAsSystem(() ->
                nodeService.exists(timerRef) && caseTimerService.isTimerValid(timerRef)
        );
    }

    /* timer start */

    @Override
    public void beforeCaseActivityStarted(final NodeRef timerRef) {
        AuthenticationUtil.runAsSystem(() -> caseTimerService.startTimer(timerRef));
    }

    @Override
    public void onCaseActivityStarted(final NodeRef timerRef) {
        AuthenticationUtil.runAsSystem(() -> {
            if (caseTimerService.isOccurred(timerRef)) {
                caseTimerService.timerOccur(timerRef);
            } else if (!caseTimerService.isActive(timerRef)) {
                caseActivityService.reset(timerRef);
            }
            return null;
        });
    }

    /* timer reset */

    @Override
    public void onCaseActivityReset(NodeRef timerRef) {
        clearTimerData(timerRef);
    }

    @Override
    public void onCaseActivityStopped(NodeRef timerRef) {
        clearTimerData(timerRef);
    }

    private void clearTimerData(final NodeRef timerRef) {
        AuthenticationUtil.runAsSystem(() -> {
            caseTimerService.stopTimer(timerRef);
            return null;
        });
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        policyComponent = (PolicyComponent) serviceRegistry.getService(AlfrescoServices.POLICY_COMPONENT);
        caseTimerService = (CaseTimerService) serviceRegistry.getService(EcosCoreServices.CASE_TIMER_SERVICE);
        caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
    }
}
