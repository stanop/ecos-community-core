package ru.citeck.ecos.cmmn.behaviour;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.cmmn.service.CaseTemplateRegistry;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.converter.ContentValueConverter;
import ru.citeck.ecos.model.ICaseModel;

import java.io.Serializable;
import java.util.*;

public class CaseTemplateContentSyncBehaviour extends AbstractBehaviour
                                              implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                                         ContentServicePolicies.OnContentUpdatePolicy {

    private static final String TXN_TEMPLATE_DATA_KEY = CaseTemplateContentSyncBehaviour.class.toString();

    private static final Map<QName, javax.xml.namespace.QName> FIELDS_MAPPING
                            = new HashMap<QName, javax.xml.namespace.QName>() {{
        put(ICaseModel.PROP_CASE_ECOS_TYPE, CMMNUtils.QNAME_CASE_ECOS_TYPE);
        put(ICaseModel.PROP_CASE_ECOS_KIND, CMMNUtils.QNAME_CASE_ECOS_KIND);
        put(ICaseModel.PROP_CASE_TYPE, CMMNUtils.QNAME_CASE_TYPE);
    }};

    private CaseTemplateRegistry registry;
    private ContentValueConverter converter;

    @Override
    protected void beforeInit() {
        setClassName(registry.getConfigNodeType());
    }

    @PolicyMethod(policy = NodeServicePolicies.OnUpdatePropertiesPolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
                                                    Map<QName, Serializable> after) {

        if (!TransactionalResourceHelper.getSet(TXN_TEMPLATE_DATA_KEY).add(nodeRef)) {
            return;
        }

        Map<javax.xml.namespace.QName, String> changedProps = new HashMap<>();

        for (QName key : FIELDS_MAPPING.keySet()) {

            Serializable valueBefore = before.get(key);
            Serializable valueAfter = after.get(key);

            if (!Objects.equals(valueBefore, valueAfter)) {
                changedProps.put(FIELDS_MAPPING.get(key),
                                 converter.convertToConfigValue(key, valueAfter));
            }
        }

        if (changedProps.size() > 0) {

            Optional<ContentData<Definitions>> configData = registry.getContentData(nodeRef);

            configData.ifPresent(d -> d.changeData(data -> {

                Map<javax.xml.namespace.QName, String> attr = data.getCase().get(0).getOtherAttributes();

                for (javax.xml.namespace.QName key : changedProps.keySet()) {

                    String newValue = changedProps.get(key);

                    if (newValue != null) {
                        attr.put(key, newValue);
                    } else {
                        attr.remove(key);
                    }
                }
            }));
        }
    }

    @PolicyMethod(policy = ContentServicePolicies.OnContentUpdatePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

        TransactionalResourceHelper.getSet(TXN_TEMPLATE_DATA_KEY).add(nodeRef);

        Optional<ContentData<Definitions>> configData = registry.getContentData(nodeRef);
        configData.flatMap(ContentData::getData).ifPresent(d -> {

            Map<javax.xml.namespace.QName, String> attr = d.getCase().get(0).getOtherAttributes();

            for (QName propQName : FIELDS_MAPPING.keySet()) {
                String templateValue = attr.get(FIELDS_MAPPING.get(propQName));
                Serializable repoValue = converter.convertToRepoValue(propQName, templateValue);
                nodeService.setProperty(nodeRef, propQName, repoValue);
            }

            //update template parent to drop config registry cache
            ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
            nodeService.setProperty(parentAssoc.getParentRef(), ICaseModel.PROP_LAST_CHANGED_DATE, new Date());
        });
    }

    @Autowired
    public void setRegistry(CaseTemplateRegistry registry) {
        this.registry = registry;
    }

    @Autowired
    @Qualifier("CMMNUtils")
    public void setValueConverter(ContentValueConverter converter) {
        this.converter = converter;
    }

}
