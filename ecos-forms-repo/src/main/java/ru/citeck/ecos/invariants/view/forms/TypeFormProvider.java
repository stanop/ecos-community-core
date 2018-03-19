package ru.citeck.ecos.invariants.view.forms;

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

import java.util.Map;

@Component
public class TypeFormProvider implements NodeViewProvider {

    @Autowired
    private NodeViewService nodeViewService;
    @Autowired
    private NamespaceService namespaceService;

    @Override
    public NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, String> params) {

        NodeView.Builder builder = new NodeView.Builder(namespaceService);

        QName typeKey = QName.resolveToQName(namespaceService, formKey);
        builder.className(typeKey);

        if (formId != null) {
            builder.id(formId);
        }
        if (mode != null) {
            builder.mode(NodeViewMode.valueOf(mode.toString()));
        }

        NodeViewDefinition view = new NodeViewDefinition();

        NodeView query = builder.build();
        if (nodeViewService.hasNodeView(query)) {
            view.nodeView = nodeViewService.getNodeView(query);
        }
        view.canBeDraft = nodeViewService.canBeDraft(typeKey);

        return view;
    }

    @Override
    public void reload() {
    }

    @Override
    public String getType() {
        return "type";
    }
}
