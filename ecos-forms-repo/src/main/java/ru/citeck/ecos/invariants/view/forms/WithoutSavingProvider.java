package ru.citeck.ecos.invariants.view.forms;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface WithoutSavingProvider {

    String FORM_ATTRIBUTES = "formAttributes";

    default Map<String, Object> process(Map<QName, Object> attributes, NamespaceService namespaceService) {
        Map<String, Object> model          = new HashMap<>();
        Map<String, String> formAttributes = new HashMap<>();

        model.put(FORM_ATTRIBUTES, formAttributes);

        if (MapUtils.isNotEmpty(attributes)) {
            for (Map.Entry<QName, Object> attribute: attributes.entrySet()) {
                String key   = attribute.getKey().toPrefixString(namespaceService);
                String value = null;

                if (attribute.getValue() instanceof List) {
                    List<?> valueAsList = (List<?>) attribute.getValue();

                    if (CollectionUtils.isNotEmpty(valueAsList)) {
                        value = valueAsList.stream()
                                           .map(Object::toString)
                                           .collect(Collectors.joining(","));
                    }
                } else {
                    value = attribute.getValue() != null
                            ? attribute.getValue().toString()
                            : null;
                }

                formAttributes.put(key, value);
            }
        }

        return model;
    }
}