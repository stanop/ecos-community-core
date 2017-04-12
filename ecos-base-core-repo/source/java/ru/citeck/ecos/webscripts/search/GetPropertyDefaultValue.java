package ru.citeck.ecos.webscripts.search;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Makarskiy
 */
public class GetPropertyDefaultValue extends DeclarativeWebScript {
    private static final String PARAM_NODE_TYPE = "nodeType";
    private static final String PARAM_PROPERTY = "property";
    private static final String KEY_DEFAULT_VALUE = "defaultValue";

    private DictionaryService dictionaryService;
    private NamespacePrefixResolver prefixResolver;

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String nodeType = req.getParameter(PARAM_NODE_TYPE);
        String property = req.getParameter(PARAM_PROPERTY);

        if (nodeType == null || property == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, PARAM_NODE_TYPE + " and " + PARAM_PROPERTY
                    + " should be set");
            return null;
        }

        QName nodeTypeQName = QName.resolveToQName(prefixResolver, nodeType);
        QName propertyQName = QName.resolveToQName(prefixResolver, property);

        PropertyDefinition propertyDefinition;
        ClassDefinition classDefinition = dictionaryService.getClass(nodeTypeQName);
        String defaultValue = null;

        for (ClassDefinition aspectDefinition : classDefinition.getDefaultAspects()) {
            propertyDefinition = getProperty(aspectDefinition, propertyQName);
            if (propertyDefinition != null) {
                defaultValue = propertyDefinition.getDefaultValue();
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put(KEY_DEFAULT_VALUE, defaultValue);
        return model;
    }

    private PropertyDefinition getProperty(ClassDefinition classDefinition, QName property) {
        Map<QName, PropertyDefinition> definitions = classDefinition.getProperties();
        return definitions.get(property);
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }
}
