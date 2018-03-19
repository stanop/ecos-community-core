package ru.citeck.ecos.forms;

import java.util.Map;

public interface EcosFormService {

    NodeViewDefinition getNodeView(String formType, String formKey);

    NodeViewDefinition getNodeView(String formType, String formKey, String formId);

    NodeViewDefinition getNodeView(String formType, String formKey, String formId, FormMode mode);

    NodeViewDefinition getNodeView(String formType, String formKey, String formId, FormMode mode,
                                   Map<String, String> params);

}
