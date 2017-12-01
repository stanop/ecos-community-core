package ru.citeck.ecos.content.config;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ClassificationModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TypeKindConfigRegistry<T> extends ContentConfigRegistry<T> {

    private QName typeField = ClassificationModel.PROP_DOCUMENT_APPLIES_TO_TYPE;
    private QName kindField = ClassificationModel.PROP_DOCUMENT_APPLIES_TO_KIND;
    private QName classField = null;

    public ConfigData<T> getConfigByNode(NodeRef nodeRef) {

        ConfigData<T> config = null;

        NodeRef type = (NodeRef) nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_TYPE);
        //case type/kind
        if (type != null) {
            NodeRef kind = (NodeRef) nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_KIND);
            config = getConfigByTypeKind(type, kind);
        }
        //alfresco type
        if (config == null) {
            QName nodeType = nodeService.getType(nodeRef);
            config = getConfigByClassName(nodeType, true);
        }
        //aspects
        if (config == null) {
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects) {
                config = getConfigByClassName(aspect, false);
                if (config != null) break;
            }
        }
        return config;
    }

    public ConfigData<T> getConfigByTypeKind(NodeRef type, NodeRef kind) {

        ConfigData<T> config = null;

        if (type != null) {

            Map<QName, Serializable> keys = new HashMap<>(2);
            keys.put(typeField, type);
            keys.put(kindField, kind);

            config = getConfig(keys);
            if (config == null && kind != null) {
                return getConfigByTypeKind(type, null);
            }
        }

        return config;
    }

    public ConfigData<T> getConfigByClassName(QName className, boolean includeParents) {

        if (classField == null) {
            return null;
        }

        Map<QName, Serializable> keys = new HashMap<>(1);
        keys.put(classField, className);
        ConfigData<T> config = getConfig(keys);

        if (config == null && includeParents) {
            ClassDefinition classDef = dictionaryService.getClass(className);
            while (classDef != null && config == null) {
                classDef = classDef.getParentClassDefinition();
                if (classDef != null) {
                    keys.put(classField, classDef.getName());
                    config = getConfig(keys);
                }
            }
        }

        return config;
    }

    public void setTypeField(QName typeField) {
        this.typeField = typeField;
    }

    public void setKindField(QName kindField) {
        this.kindField = kindField;
    }

    public void setClassField(QName classField) {
        this.classField = classField;
    }
}
