package ru.citeck.ecos.behavior.history;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.IdocsModel;

public class UseNewHistoryFlagBehaviour extends AbstractBehaviour
                                        implements NodeServicePolicies.OnCreateNodePolicy {

    private HistoryService historyService;

    @Autowired
    public UseNewHistoryFlagBehaviour(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    protected void beforeInit() {
        setClassName(HistoryModel.ASPECT_HISTORICAL);
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        boolean remoteEnabled = historyService.isEnabledRemoteHistoryService();
        nodeService.setProperty(childAssocRef.getChildRef(), IdocsModel.PROP_USE_NEW_HISTORY, remoteEnabled);
    }
}
