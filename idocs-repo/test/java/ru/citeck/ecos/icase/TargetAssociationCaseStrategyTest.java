package ru.citeck.ecos.icase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.ICaseModel;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class TargetAssociationCaseStrategyTest extends BaseCaseStrategyTest {

    @Override
    protected NodeRef createConfigNode() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ICaseModel.PROP_CASE_CLASS, ICaseModel.ASPECT_CASE);
        properties.put(ICaseModel.PROP_ELEMENT_TYPE, ContentModel.PROP_CONTENT);
        properties.put(ICaseModel.PROP_ASSOC_TYPE, "target");
        properties.put(ICaseModel.PROP_ASSOC_NAME, ContentModel.ASSOC_ATTACHMENTS);
        
        return nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN,
                QName.createQName(ICaseModel.NAMESPACE, "config"),
                ICaseModel.TYPE_ASSOC_CONFIG, properties).getChildRef();
        
    }

    @Override
    protected CaseElementDAO createStrategy() {
        return applicationContext.getBean("strategy.associationCaseElement",
                AssociationCaseElementDAOImpl.class);
    }

}
