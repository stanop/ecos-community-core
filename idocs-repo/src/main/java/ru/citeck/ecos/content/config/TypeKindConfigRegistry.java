package ru.citeck.ecos.content.config;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ClassificationModel;

import java.io.Serializable;
import java.util.*;

/**
 * TypeKind config registry helps to search configs by EcoS type and kind
 * @param <T> type of parsed content data
 *
 * @author Pavel Simonov
 */
public class TypeKindConfigRegistry<T> extends ContentConfigRegistry<T> {

    private QName typeField = ClassificationModel.PROP_DOCUMENT_APPLIES_TO_TYPE;
    private QName kindField = ClassificationModel.PROP_DOCUMENT_APPLIES_TO_KIND;
    private QName classField = null;

    /**
     * Get configs by EcoS type kind
     * Keys to search in order of priority:
     * 1) EcoS type and EcoS kind
     * 2) EcoS type and EcoS kind is empty
     */
    public List<ConfigData<T>> getConfigsByTypeKind(NodeRef type, NodeRef kind) {

        List<ConfigData<T>> configs = Collections.emptyList();

        if (type != null) {

            Map<QName, Serializable> keys = new HashMap<>(2);
            keys.put(typeField, type);
            keys.put(kindField, kind);

            configs = getConfigs(keys);
            if (configs.isEmpty() && kind != null) {
                return getConfigsByTypeKind(type, null);
            }
        }

        return configs;
    }

    /**
     *  Get configs by EcoS type kind or class of node
     *  Keys to search in order of priority:
     *  1) EcoS type and EcoS kind
     *  2) EcoS type and EcoS kind is empty
     *  3) Alfresco type
     *  4) Alfresco aspect
     * */
    public List<ConfigData<T>> getConfigsByNodeTKC(NodeRef nodeRef) {

        List<ConfigData<T>> configs = Collections.emptyList();

        NodeRef type = (NodeRef) nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_TYPE);
        //case type/kind
        if (type != null) {
            NodeRef kind = (NodeRef) nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_KIND);
            configs = getConfigsByTypeKind(type, kind);
        }
        //alfresco type
        if (configs.isEmpty()) {
            QName nodeType = nodeService.getType(nodeRef);
            configs = getConfigsByClassName(nodeType, true);
        }
        //aspects
        if (configs.isEmpty()) {
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects) {
                configs = getConfigsByClassName(aspect, false);
                if (!configs.isEmpty()) break;
            }
        }
        return configs;
    }

    /**
     * Get config by alfresco class
     * @param className alfresco type or aspect name
     */
    public List<ConfigData<T>> getConfigsByClassName(QName className, boolean includeParents) {

        if (classField == null) {
            return Collections.emptyList();
        }

        Map<QName, Serializable> keys = new HashMap<>(1);
        keys.put(classField, className);
        List<ConfigData<T>> configs = getConfigs(keys);

        if (configs.isEmpty() && includeParents) {
            ClassDefinition classDef = dictionaryService.getClass(className);
            while (classDef != null && configs.isEmpty()) {
                classDef = classDef.getParentClassDefinition();
                if (classDef != null) {
                    keys.put(classField, classDef.getName());
                    configs = getConfigs(keys);
                }
            }
        }

        return configs;
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
