package ru.citeck.ecos.behavior.menu;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.menu.MenuService;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.utils.TransactionUtils;

import java.io.Serializable;
import java.util.Map;

public class ResetMenuCacheBehaviour extends AbstractBehaviour {

    public static String TXN_ACTION_KEY = ResetMenuCacheBehaviour.class.getName();

    public static QName TYPE_JOURNAL = JournalsModel.TYPE_JOURNAL;
    public static QName TYPE_SITE = SiteModel.TYPE_SITE;

    private MenuService menuService;

    @PolicyMethod(policy = NodeServicePolicies.OnUpdatePropertiesPolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  classField = "TYPE_JOURNAL")
    public void onUpdateJournalProperties(NodeRef nodeRef,
                                          Map<QName, Serializable> before,
                                          Map<QName, Serializable> after) {
        updateCache();
    }


    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  classField = "TYPE_SITE")
    public void onCreateSiteNode(ChildAssociationRef childAssocRef) {
        updateCache();
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
                  runAsSystem = true, frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  classField = "TYPE_JOURNAL")
    public void onCreateJournalNode(ChildAssociationRef childAssocRef) {
        updateCache();
    }

    private void updateCache() {
        TransactionUtils.processBatchAfterCommit(TXN_ACTION_KEY, "", (empty) -> {
            menuService.resetCache();
        }, (error) -> {
            //do nothing
        });
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
