package ru.citeck.ecos.forms;

import org.alfresco.service.namespace.QName;

import java.util.Map;

public interface EcosFormService {

    NodeViewDefinition getNodeView(String formType, String formKey);

    NodeViewDefinition getNodeView(String formType, String formKey, String formId);

    NodeViewDefinition getNodeView(String formType, String formKey, String formId, FormMode mode);

    NodeViewDefinition getNodeView(String formType, String formKey, String formId, FormMode mode,
                                   Map<String, Object> params);

    Map<String, Object> saveNodeView(String formType, String formKey, String formId, FormMode mode,
                                     Map<String, Object> params, Map<QName, Object> attributes);

    boolean hasNodeView(String formType, String formKey, String formId, FormMode mode, Map<String, Object> params);
}
