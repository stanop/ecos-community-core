package ru.citeck.ecos.invariants.view.forms;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;

import java.util.HashMap;
import java.util.Map;

@Component
public class TypeFormProvider implements NodeViewProvider {

    private static final String MODEL_NODE = "nodeRef";

    @Autowired
    private NodeViewService nodeViewService;
    @Autowired
    private NamespaceService namespaceService;

    @Override
    public NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {

        QName type = QName.resolveToQName(namespaceService, formKey);
        NodeView query = getViewQuery(type, formId, mode);

        NodeViewDefinition view = new NodeViewDefinition();
        if (nodeViewService.hasNodeView(query)) {
            view.nodeView = nodeViewService.getNodeView(query);
        }
        view.canBeDraft = nodeViewService.canBeDraft(type);

        return view;
    }

    @Override
    public Map<String, Object> saveNodeView(String formKey, String formId, FormMode mode,
                                            Map<String, Object> params, Map<QName, Object> attributes) {

        QName typeName = QName.resolveToQName(namespaceService, formKey);
        NodeRef nodeRef = nodeViewService.saveNodeView(typeName, formId, attributes, params);

        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_NODE, nodeRef.toString());
        return model;
    }

    @Override
    public boolean hasNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {
        QName type = QName.resolveToQName(namespaceService, formKey);
        NodeView query = getViewQuery(type, formId, mode);
        return nodeViewService.hasNodeView(query);
    }

    private NodeView getViewQuery(QName type, String formId, FormMode mode) {

        NodeView.Builder builder = new NodeView.Builder(namespaceService);

        builder.className(type);

        if (formId != null) {
            builder.id(formId);
        }
        if (mode != null) {
            builder.mode(NodeViewMode.valueOf(mode.toString()));
        }
        return builder.build();
    }


    @Override
    public void reload() {
    }

    @Override
    public String getType() {
        return "type";
    }
}
