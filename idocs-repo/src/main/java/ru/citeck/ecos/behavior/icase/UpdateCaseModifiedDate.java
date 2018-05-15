package ru.citeck.ecos.behavior.icase;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.icase.element.CaseElementPolicies;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.Date;

public class UpdateCaseModifiedDate extends AbstractBehaviour
                                    implements CaseElementPolicies.OnCaseElementUpdatePolicy,
                                               CaseElementPolicies.OnCaseElementAddPolicy,
                                               CaseElementPolicies.OnCaseElementRemovePolicy {

    private static final String TXN_NODES_TO_UPDATE = UpdateCaseModifiedDate.class.getName();

    private NodeService nodeService;

    @Override
    protected void beforeInit() {
        setClassName(ICaseModel.ASPECT_CASE);
        nodeService = serviceRegistry.getNodeService();
    }

    @PolicyMethod(policy = CaseElementPolicies.OnCaseElementAddPolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
    public void onCaseElementAdd(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        updateNode(caseRef);
    }

    @PolicyMethod(policy = CaseElementPolicies.OnCaseElementUpdatePolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
    public void onCaseElementUpdate(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        updateNode(caseRef);
    }

    @PolicyMethod(policy = CaseElementPolicies.OnCaseElementRemovePolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
    public void onCaseElementRemove(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        updateNode(caseRef);
    }

    private void updateNode(NodeRef nodeRef) {
        TransactionUtils.processAfterBehaviours(TXN_NODES_TO_UPDATE, nodeRef, nodeRefToProcess -> {
            if (nodeService.exists(nodeRefToProcess)) {
                nodeService.setProperty(nodeRefToProcess, ICaseModel.PROP_LAST_CHANGED_DATE, new Date());
            }
        });
    }
}
