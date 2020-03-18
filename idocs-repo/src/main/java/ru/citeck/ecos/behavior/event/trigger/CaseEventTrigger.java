package ru.citeck.ecos.behavior.event.trigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.service.CiteckServices;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
@Component
@DependsOn("idocs.dictionaryBootstrap")
public class CaseEventTrigger implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private PolicyComponent policyComponent;
    private CaseActivityEventService caseActivityEventService;

    private int order = 200;

    @Autowired
    public CaseEventTrigger(ServiceRegistry serviceRegistry) {
        this.policyComponent = serviceRegistry.getPolicyComponent();
        this.caseActivityEventService = (CaseActivityEventService) serviceRegistry
            .getService(CiteckServices.CASE_ACTIVITY_EVENT_SERVICE);
    }

    @PostConstruct
    public void init() {
        OrderedBehaviour behaviour;
        QName type = ICaseModel.ASPECT_CASE;
        NotificationFrequency frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT;

        behaviour = new OrderedBehaviour(this, "onUpdateProperties", frequency, order);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, type, behaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        RecordRef caseRef = RecordRef.valueOf(nodeRef.toString());
        ActivityRef activityRef = ActivityRef.of(CaseServiceType.ALFRESCO, caseRef, ActivityRef.ROOT_ID);
        caseActivityEventService.fireEvent(activityRef, ICaseEventModel.CONSTR_CASE_PROPERTIES_CHANGED);
    }
}
