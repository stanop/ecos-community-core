package ru.citeck.ecos.forms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EcosFormServiceImpl implements EcosFormService {

    private Map<String, NodeViewProvider> nodeViewProviders = new ConcurrentHashMap<>();

    public NodeViewDefinition getNodeView(String formType, String formKey) {
        return getNodeView(formType, formKey, null);
    }

    public NodeViewDefinition getNodeView(String formType, String formKey, String formId) {
        return getNodeView(formType, formKey, formId, null);
    }

    public NodeViewDefinition getNodeView(String formType, String formKey, String formId, FormMode mode) {
        return getNodeView(formType, formKey, formId, mode, Collections.emptyMap());
    }

    public NodeViewDefinition getNodeView(String formType, String formKey, String formId, FormMode mode,
                                          Map<String, String> params) {
        NodeViewProvider provider = nodeViewProviders.get(formType);
        return provider.getNodeView(formKey, formId, mode, params);
    }

    @Autowired
    public void setNodeViewProviders(List<? extends NodeViewProvider> providers) {
        for (NodeViewProvider provider : providers) {
            nodeViewProviders.put(provider.getType(), provider);
        }
    }
}
