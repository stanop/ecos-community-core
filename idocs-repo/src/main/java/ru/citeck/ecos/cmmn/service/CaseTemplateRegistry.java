package ru.citeck.ecos.cmmn.service;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.ICaseModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Simonov
 */
@Component
public class CaseTemplateRegistry {

    private final Logger logger = LoggerFactory.getLogger(CaseTemplateRegistry.class);

    private static final String SCRIPT_ENGINE = "javascript";

    private Map<QName, NodeRef> templatesByClass = new ConcurrentHashMap<>();
    private Map<TypeKind, NodeRef> templatesByTypeKind = new ConcurrentHashMap<>();
    private Map<NodeRef, Definitions> definitionsByTemplateRef = new ConcurrentHashMap<>();

    private NodeService nodeService;
    private ScriptService scriptService;
    private DictionaryService dictionaryService;

    public void addDefinition(NodeRef type, NodeRef kind, QName className, NodeRef templateRef, Definitions definition) {

        definitionsByTemplateRef.put(templateRef, definition);
        if (type != null) {
            templatesByTypeKind.put(new TypeKind(type, kind), templateRef);
        } else if (className != null){
            templatesByClass.put(className, templateRef);
        } else {
            throw new IllegalArgumentException("Either type or className should be specified");
        }
    }

    public Definitions getDefinitionForCase(NodeRef caseRef) {
        NodeRef templateRef = getTemplateRefForCase(caseRef);
        if (templateRef != null && evalCondition(templateRef, caseRef)) {
            return definitionsByTemplateRef.get(templateRef);
        }
        return null;
    }

    private boolean evalCondition(NodeRef templateRef, NodeRef caseNode) {
        if (templateRef == null) {
            return false;
        }
        String conditionScript = (String) nodeService.getProperty(templateRef, ICaseModel.PROP_CONDITION);
        if (StringUtils.isNotBlank(conditionScript)) {
            Map<String, Object> model = new HashMap<>();
            model.put("caseNode", caseNode);
            Object scriptResult = scriptService.executeScriptString(SCRIPT_ENGINE, conditionScript, model);
            return Boolean.TRUE.equals(scriptResult);
        }
        return true;
    }

    public NodeRef getTemplateRefForCase(NodeRef caseRef) {

        NodeRef templateRef = null;

        NodeRef type = (NodeRef) nodeService.getProperty(caseRef, ClassificationModel.PROP_DOCUMENT_TYPE);
        //case type/kind
        if (type != null) {
            NodeRef kind = (NodeRef) nodeService.getProperty(caseRef, ClassificationModel.PROP_DOCUMENT_KIND);
            templateRef = getTemplateRef(type, kind);
        }
        //alfresco type
        if (templateRef == null) {
            QName nodeType = nodeService.getType(caseRef);
            templateRef = getTemplateRef(nodeType, true);
        }
        //aspects
        if (templateRef == null) {
            Set<QName> aspects = nodeService.getAspects(caseRef);
            for (QName aspect : aspects) {
                templateRef = getTemplateRef(aspect, false);
                if (templateRef != null) break;
            }
        }
        return templateRef;
    }

    public Definitions getDefinition(NodeRef type, NodeRef kind) {
        NodeRef templateRef = getTemplateRef(type, kind);
        return templateRef != null ? definitionsByTemplateRef.get(templateRef) : null;
    }

    public Definitions getDefinition(QName className, boolean includeParents) {
        NodeRef templateRef = getTemplateRef(className, includeParents);
        return templateRef != null ? definitionsByTemplateRef.get(templateRef) : null;
    }

    public NodeRef getTemplateRef(NodeRef type, NodeRef kind) {
        NodeRef templateRef = null;
        if (type != null) {
            TypeKind key = new TypeKind(type, kind);
            templateRef = templatesByTypeKind.get(key);
            if (templateRef == null) {
                key = new TypeKind(type, null);
                templateRef = templatesByTypeKind.get(key);
            }
            if (templateRef != null && !nodeService.exists(templateRef)) {
                logger.error("Template nodeRef doesn't exists: " + templateRef + " key: " + key);
                templateRef = null;
            }
        }
        return templateRef;
    }

    public NodeRef getTemplateRef(QName className, boolean includeParents) {
        QName key = className;
        NodeRef templateRef = templatesByClass.get(key);
        if (templateRef == null && includeParents) {
            ClassDefinition classDef = dictionaryService.getType(className);
            if (classDef == null) {
                classDef = dictionaryService.getAspect(className);
            }
            while (classDef != null && templateRef == null) {
                classDef = classDef.getParentClassDefinition();
                key = classDef.getName();
                templateRef = templatesByClass.get(key);
            }
        }
        if (templateRef != null && !nodeService.exists(templateRef)) {
            logger.error("Template nodeRef doesn't exists: " + templateRef + " key: " + key);
            templateRef = null;
        }
        return templateRef;
    }

    public static class TypeKind {

        public final NodeRef type;
        public final NodeRef kind;

        TypeKind(NodeRef type, NodeRef kind) {
            this.type = type;
            this.kind = kind;
        }

        @Override
        public String toString() {
            return "TypeKind{type=" + type + ", kind=" + kind + "}";
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypeKind typeKind = (TypeKind) o;

            return Objects.equals(type, typeKind.type)
                && Objects.equals(kind, typeKind.kind);
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (kind != null ? kind.hashCode() : 0);
            return result;
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();
        scriptService = (ScriptService) serviceRegistry.getService(ServiceRegistry.SCRIPT_SERVICE);
    }
}
