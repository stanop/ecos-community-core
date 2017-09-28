package ru.citeck.ecos.pred;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.utils.DictionaryUtils;

public class ScriptPredicateEvaluator extends AbstractPredicateEvaluator {
    
    private static final String MODEL_PREDICATE = "predicate";
    
    private ScriptService scriptService;
    
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private TemplateService templateService;

    private String scriptEngine;
    private String templatesRootPath;
    private String templatesExtensionRootPath;

    @Override
    public boolean evaluate(NodeRef predicate, Map<String, Object> model) {
        String expression = getScriptExpression(predicate);
        if(expression == null)
            throw new IllegalArgumentException("Can not find predicate template for " + predicate + " (type " + nodeService.getType(predicate) + ")");
        Object result = scriptService.executeScriptString(scriptEngine, expression, model);
        return Boolean.TRUE.equals(result);
    }

    private String getScriptExpression(NodeRef predicate) {
        List<QName> predicateTypes = DictionaryUtils.getAllNodeTypeNames(predicate, nodeService, dictionaryService);
        Map<String, NodeRef> model = Collections.singletonMap(MODEL_PREDICATE, predicate);
        for(QName predicateType : predicateTypes) {
            try {
                return templateService.processTemplate(getTemplatePath(templatesExtensionRootPath, predicateType), model);
            } catch(RuntimeException e) {
                // ignore
            }
            try {
                return templateService.processTemplate(getTemplatePath(templatesRootPath, predicateType), model);
            } catch(RuntimeException e) {
                // ignore
            }
        }
        return null;
    }

    private String getTemplatePath(String rootPath, QName predicateType) {
        String prefixString = predicateType.toPrefixString(namespaceService).replace(':', '_');
        return rootPath + "/" + prefixString + ".ftl";
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setScriptEngine(String engine) {
        this.scriptEngine = engine;
    }

    public void setTemplatesRootPath(String templatesRootPath) {
        this.templatesRootPath = templatesRootPath;
    }

    public void setTemplatesExtensionRootPath(String templatesExtensionRootPath) {
        this.templatesExtensionRootPath = templatesExtensionRootPath;
    }

}
