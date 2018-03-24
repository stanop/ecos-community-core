package ru.citeck.ecos.invariants.view.forms;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NodeRefFormProvider implements NodeViewProvider {

    private static final String PARAM_NODEREF_ATTR = "nodeRefAttr";
    private static final String MODEL_NODE = "nodeRef";

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
    public NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {

        NodeViewDefinition view = new NodeViewDefinition();

        NodeRef nodeRef = getNodeRef(formKey, params);
        NodeView query = getViewQuery(nodeRef, formId, mode, params);

        if (nodeViewService.hasNodeView(query)) {
            view.nodeView = nodeViewService.getNodeView(query);
        }
        view.canBeDraft = nodeViewService.canBeDraft(nodeRef);

        return view;
    }

    @Override
    public Map<String, Object> saveNodeView(String formKey, String formId, FormMode mode,
                                            Map<String, Object> params, Map<QName, Object> attributes) {

        ParameterCheck.mandatory("attributes", attributes);

        NodeRef nodeRef = new NodeRef(formKey);
        nodeViewService.saveNodeView(nodeRef, formId, attributes, params);

        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_NODE, nodeRef.toString());
        return model;
    }

    @Override
    public boolean hasNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {
        NodeRef nodeRef = getNodeRef(formKey, params);
        NodeView query = getViewQuery(nodeRef, formId, mode, params);
        return nodeViewService.hasNodeView(query);
    }

    private NodeView getViewQuery(NodeRef nodeRef, String formId, FormMode mode, Map<String, Object> params) {

        NodeView.Builder builder = new NodeView.Builder(namespaceService);

        builder.className(nodeService.getType(nodeRef));
        builder.templateParams(params);

        if (formId != null) {
            builder.id(formId);
        }
        if (mode != null) {
            builder.mode(NodeViewMode.valueOf(mode.toString()));
        }

        return builder.build();
    }

    private NodeRef getNodeRef(String formKey, Map<String, Object> params) {

        if (!NodeRef.isNodeRef(formKey)) {
            throw new IllegalArgumentException("formKey must contain nodeRef. value = " + formKey);
        }
        NodeRef nodeRef = new NodeRef(formKey);
        if (!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("Node " + nodeRef + " does not exist");
        }
        String nodeRefAttrParam = (String) params.get(PARAM_NODEREF_ATTR);
        if (StringUtils.isNotBlank(nodeRefAttrParam)) {
            nodeRef = getNodeRefByAttribute(nodeRef, nodeRefAttrParam);
            if (nodeRef == null) {
                throw new IllegalArgumentException("Attribute " + nodeRefAttrParam +
                                                   " of node " + formKey + " does not exist");
            }
        }

        return nodeRef;
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