package ru.citeck.ecos.forms;

import org.alfresco.service.namespace.QName;
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
                                          Map<String, Object> params) {
        if (params == null) {
            params = Collections.emptyMap();
        }
        NodeViewProvider provider = nodeViewProviders.get(formType);
        return provider.getNodeView(formKey, formId, mode, params);
    }

    @Override
    public Map<String, Object> saveNodeView(String formType, String formKey, String formId, FormMode mode,
                                            Map<String, Object> params, Map<QName, Object> attributes) {
        if (params == null) {
            params = Collections.emptyMap();
        }
        NodeViewProvider provider = nodeViewProviders.get(formType);
        return provider.saveNodeView(formKey, formId, mode, params, attributes);
    }

    @Override
    public boolean hasNodeView(String formType, String formKey, String formId, FormMode mode,
                               Map<String, Object> params) {
        if (params == null) {
            params = Collections.emptyMap();
        }
        NodeViewProvider provider = nodeViewProviders.get(formType);
        return provider.hasNodeView(formKey, formId, mode, params);
    }

    @Autowired
    public void setNodeViewProviders(List<? extends NodeViewProvider> providers) {
        for (NodeViewProvider provider : providers) {
            nodeViewProviders.put(provider.getType(), provider);
        }
    }
}
