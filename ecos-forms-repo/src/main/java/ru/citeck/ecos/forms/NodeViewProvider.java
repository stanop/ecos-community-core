package ru.citeck.ecos.forms;

import java.util.Map;

public interface NodeViewProvider {

    NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, String> params);

    void reload();

    String getType();
}
