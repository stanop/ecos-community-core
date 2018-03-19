package ru.citeck.ecos.invariants.view.forms;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;

import java.util.List;
import java.util.Map;

@Component
public class NodeRefFormProvider implements NodeViewProvider {

    private static final String PARAM_NODEREF_ATTR = "nodeRefAttr";

    @Autowired
    private NodeService nodeService;
    @Autowired
    private NodeViewService nodeViewService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private NodeAttributeService nodeAttributeService;

    @Override
    public String getType() {
        return "nodeRef";
    }

    @Override
    public NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, String> params) {

        NodeView.Builder builder = new NodeView.Builder(namespaceService);

        if (!NodeRef.isNodeRef(formKey)) {
            throw new IllegalArgumentException("formKey must contain nodeRef. value = " + formKey);
        }
        NodeRef nodeRef = new NodeRef(formKey);
        if (!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("Node " + nodeRef + " does not exist");
        }
        String nodeRefAttrParam = params.get(PARAM_NODEREF_ATTR);
        if (StringUtils.isNotBlank(nodeRefAttrParam)) {
            nodeRef = getNodeRefByAttribute(nodeRef, nodeRefAttrParam);
            if (nodeRef == null) {
                throw new IllegalArgumentException("Attribute " + nodeRefAttrParam +
                                                   " of node " + formKey + " does not exist");
            }
        }
        builder.className(nodeService.getType(nodeRef));

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
        view.canBeDraft = nodeViewService.canBeDraft(nodeRef);

        return view;
    }

    private NodeRef getNodeRefByAttribute(NodeRef nodeRef, String attribute) {

        QName attrQName = QName.resolveToQName(namespaceService, attribute);
        Object value = nodeAttributeService.getAttribute(nodeRef, attrQName);

        if (value instanceof List) {
            List<?> attributeList = (List<?>) value;
            if (attributeList.size() > 0) {
                value = attributeList.get(0);
            }
        }

        return value instanceof NodeRef ? (NodeRef) value : null;
    }

    @Override
    public void reload() {}
}