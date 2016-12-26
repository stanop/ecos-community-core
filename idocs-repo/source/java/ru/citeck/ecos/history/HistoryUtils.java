package ru.citeck.ecos.history;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.HistoryModel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrey.kozlov on 20.12.2016.
 */
public class HistoryUtils {

    public static Map<QName, Serializable> eventProperties(Serializable name, Serializable assocDocument, Serializable propertyName, Serializable propertyValue, Serializable taskComment, Serializable propTargetNodeType, Serializable propTargetNodeKind) {
        Map<QName, Serializable> eventProperties = new HashMap<QName, Serializable>(7);
        eventProperties.put(HistoryModel.PROP_NAME, name);
        eventProperties.put(HistoryModel.ASSOC_DOCUMENT, assocDocument);
        eventProperties.put(HistoryModel.PROP_PROPERTY_NAME, propertyName);
        if (propertyValue != null) {
            eventProperties.put(HistoryModel.PROP_PROPERTY_VALUE, propertyValue);
        }
        eventProperties.put(HistoryModel.PROP_TASK_COMMENT, taskComment);
        if (propertyValue != null) {
            eventProperties.put(HistoryModel.PROP_TARGET_NODE_TYPE, propTargetNodeType);
        }
        if (propertyValue != null) {
            eventProperties.put(HistoryModel.PROP_TARGET_NODE_KIND, propTargetNodeKind);
        }
        return eventProperties;
    }


    public static String getKeyValue(QName qName, DictionaryService dictionaryService) {
        String modelName = dictionaryService.getProperty(qName).getModel().getName().getPrefixString().replace(":", "_");
        String propName = dictionaryService.getProperty(qName).getName().getPrefixString().replace(":", "_");
        return I18NUtil.getMessage(modelName + ".property." + propName + ".title");
    }

    public static Object getKeyValue(QName qName, Object constraint, DictionaryService dictionaryService, NodeService nodeService) {
        if (DataTypeDefinition.BOOLEAN.equals(dictionaryService.getProperty(qName).getDataType().getName())) {
            if (constraint == null || constraint.equals(false)) {
                return "Нет";
            } else {
                return  "Да";
            }
        }
        if (constraint == null) {
            return "";
        }
        if (DataTypeDefinition.DATE.equals(dictionaryService.getProperty(qName).getDataType().getName())) {
            return new SimpleDateFormat("dd/MM/yyyy").format(constraint);
        }
        if (ClassificationModel.PROP_DOCUMENT_KIND.equals(qName)) {
            return nodeService.getProperty((NodeRef) constraint, ContentModel.PROP_NAME);
        }
        if (dictionaryService.getProperty(qName).getConstraints().size() > 0) {
            String localName = dictionaryService.getProperty(qName).getConstraints().get(0).getConstraint().getShortName().replace(":", "_");
            return I18NUtil.getMessage("listconstraint." + localName + "." + constraint);
        } else {
            return constraint;
        }
    }

    public static String getChangeValue(NodeRef nodeRef, NodeService nodeService) {
        if (ContentModel.TYPE_PERSON.equals(nodeService.getType(nodeRef))) {
            return nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME)
                    + " " + nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME);
        } else {
            return String.valueOf(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE) != null
                    ? nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)
                    : nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
        }
    }

    public static String getAssocKeyValue(QName qName, DictionaryService dictionaryService) {
        String modelName = dictionaryService.getAssociation(qName).getModel().getName().getPrefixString().replace(":", "_");
        String propName = dictionaryService.getAssociation(qName).getName().getPrefixString().replace(":", "_");
        return I18NUtil.getMessage(modelName + ".association." + propName + ".title");
    }

}
