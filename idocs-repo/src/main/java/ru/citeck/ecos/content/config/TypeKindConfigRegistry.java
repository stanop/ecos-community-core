package ru.citeck.ecos.content.config;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.ClassificationModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
     *  Get config by EcoS type kind or class of node
     *  Keys to search in order of priority:
     *  1) EcoS type and EcoS kind
     *  2) EcoS type and EcoS kind is empty
     *  3) Alfresco type
     *  4) Alfresco aspect
    * */
    public Optional<ConfigData<T>> getConfigByNodeTKC(NodeRef nodeRef) {

        Optional<ConfigData<T>> config = Optional.empty();

        NodeRef type = (NodeRef) nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_TYPE);
        //case type/kind
        if (type != null) {
            NodeRef kind = (NodeRef) nodeService.getProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_KIND);
            config = getConfigByTypeKind(type, kind);
        }
        //alfresco type
        if (!config.isPresent()) {
            QName nodeType = nodeService.getType(nodeRef);
            config = getConfigByClassName(nodeType, true);
        }
        //aspects
        if (!config.isPresent()) {
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects) {
                config = getConfigByClassName(aspect, false);
                if (config.isPresent()) break;
            }
        }
        return config;
    }

    /**
     * Get config by EcoS type kind
     * Keys to search in order of priority:
     * 1) EcoS type and EcoS kind
     * 2) EcoS type and EcoS kind is empty
     */
    public Optional<ConfigData<T>> getConfigByTypeKind(NodeRef type, NodeRef kind) {

        Optional<ConfigData<T>> config = Optional.empty();

        if (type != null) {

            Map<QName, Serializable> keys = new HashMap<>(2);
            keys.put(typeField, type);
            keys.put(kindField, kind);

            config = getConfig(keys);
            if (!config.isPresent() && kind != null) {
                return getConfigByTypeKind(type, null);
            }
        }

        return config;
    }

    /**
     * Get config by alfresco class
     * @param className alfresco type or aspect name
     */
    public Optional<ConfigData<T>> getConfigByClassName(QName className, boolean includeParents) {

        if (classField == null) {
            return Optional.empty();
        }

        Map<QName, Serializable> keys = new HashMap<>(1);
        keys.put(classField, className);
        Optional<ConfigData<T>> config = getConfig(keys);

        if (!config.isPresent() && includeParents) {
            ClassDefinition classDef = dictionaryService.getClass(className);
            while (classDef != null && !config.isPresent()) {
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
