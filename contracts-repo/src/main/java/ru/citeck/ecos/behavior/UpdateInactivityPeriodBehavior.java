package ru.citeck.ecos.behavior;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.model.BpmModel;
import ru.citeck.ecos.model.SecurityWorkflowModel;

import java.io.Serializable;
import java.util.Map;

import static org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import static org.alfresco.repo.policy.Behaviour.NotificationFrequency.TRANSACTION_COMMIT;

public class UpdateInactivityPeriodBehavior extends AbstractBehaviour implements OnUpdatePropertiesPolicy {

    private static final QName BPM_STATUS_PROP = QName.createQName(BpmModel.BPM_NAMESPASE, "status");
    
    private static final String COMPLETED_STATUS = "Completed";
    
    @Override
    protected void beforeInit() {
        setClassName(SecurityWorkflowModel.TYPE_INCOME_PACKAGE_TASK);
    }

    @Override
    @PolicyMethod(policy = OnUpdatePropertiesPolicy.class, frequency = TRANSACTION_COMMIT, runAsSystem = true)
    public void onUpdateProperties(NodeRef node, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String statusBefore = (String) before.get(BPM_STATUS_PROP);
        String statusAfter  = (String) after.get(BPM_STATUS_PROP);

        if (!COMPLETED_STATUS.equals(statusBefore) && COMPLETED_STATUS.equals(statusAfter)) {
            try {
                nodeService.removeProperty(node, QName.createQName(SecurityWorkflowModel.SAMWF_NAMESPACE, "inactivityPeriod"));
            } catch (Exception exc) {
                logger.error("Error while removing a property \"inactivityPeriod\"", exc);
            }
        }
    }
}