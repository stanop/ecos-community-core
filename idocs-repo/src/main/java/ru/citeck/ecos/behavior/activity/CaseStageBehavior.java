package ru.citeck.ecos.behavior.activity;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.StagesModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class CaseStageBehavior implements CaseActivityPolicies.BeforeCaseActivityStartedPolicy {

    private static final Log log = LogFactory.getLog(CaseStageBehavior.class);

    private CaseActivityService caseActivityService;
    private CaseStatusService caseStatusService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.BeforeCaseActivityStartedPolicy.QNAME,
                StagesModel.TYPE_STAGE,
                new ChainingJavaBehaviour(
                        this,
                        "beforeCaseActivityStarted",
                         Behaviour.NotificationFrequency.EVERY_EVENT
                )
        );
    }

    @Override
    public void beforeCaseActivityStarted(NodeRef stageRef) {
        if (!nodeService.exists(stageRef)) {
            return;
        }
        String documentStatus = (String) nodeService.getProperty(stageRef, StagesModel.PROP_DOCUMENT_STATUS);
        if (documentStatus != null && !documentStatus.isEmpty()) {
            NodeRef document = caseActivityService.getDocument(stageRef);
            nodeService.setProperty(document, IdocsModel.PROP_DOCUMENT_STATUS, documentStatus);
        }
        List<NodeRef> nodeRefs = RepoUtils.getTargetAssoc(stageRef, StagesModel.ASSOC_CASE_STATUS, nodeService);
        if (nodeRefs != null && nodeRefs.size() != 0) {
            NodeRef caseStatusRef = nodeRefs.get(0);
            NodeRef document = caseActivityService.getDocument(stageRef);
            caseStatusService.setStatus(document, caseStatusRef);
        }
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }


    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }
}
