package ru.citeck.ecos.icase;

import org.junit.*;

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
// The problem with key-property strategy testing is that it relies on index to get records.
// So after modifications the index should be first updated.
// The correct test should somehow deal with transactions.
// So it is ignored for now, as key-property strategy was never used.
@Ignore
public class KeyPropertyCaseStrategyTest extends BaseCaseStrategyTest {

    @Override
    protected NodeRef createConfigNode() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ICaseModel.PROP_CASE_CLASS, ICaseModel.ASPECT_CASE);
        properties.put(ICaseModel.PROP_ELEMENT_TYPE, ContentModel.PROP_CONTENT);
        properties.put(ICaseModel.PROP_CASE_KEY, ContentModel.PROP_NAME);
        properties.put(ICaseModel.PROP_ELEMENT_KEY, ContentModel.PROP_AUTHOR);
        
        return nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN,
                QName.createQName(ICaseModel.NAMESPACE, "config"),
                ICaseModel.TYPE_KEY_PROP_CONFIG, properties).getChildRef();
    }

    @Override
    protected CaseElementDAO createStrategy() {
        return applicationContext.getBean("strategy.keyPropertyCaseElement",
                KeyPropertyCaseElementDAOImpl.class);
    }

}
