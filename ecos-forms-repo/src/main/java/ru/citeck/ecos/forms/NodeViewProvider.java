package ru.citeck.ecos.forms;

import org.alfresco.service.namespace.QName;

import java.util.Map;

public interface NodeViewProvider {

    NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params);

    Map<String, Object> saveNodeView(String formKey, String formId, FormMode mode,
                                     Map<String, Object> params, Map<QName, Object> attributes);

    boolean hasNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params);

    void reload();

    String getType();
}
