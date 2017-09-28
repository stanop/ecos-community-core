package ru.citeck.ecos.behavior.activity;

import org.alfresco.repo.policy.Behaviour.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies.*;
import ru.citeck.ecos.model.ActivityModel;

@Component
public class CaseActivityAutoEventsBehaviour extends AbstractBehaviour implements OnChildrenIndexChangedPolicy {

    @Override
    protected void beforeInit() {
        setClassName(ActivityModel.ASPECT_HAS_ACTIVITIES);
    }

    @PolicyMethod(policy = OnChildrenIndexChangedPolicy.class,
                  frequency = NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onChildrenIndexChanged(NodeRef activityRef) {





    }
}
