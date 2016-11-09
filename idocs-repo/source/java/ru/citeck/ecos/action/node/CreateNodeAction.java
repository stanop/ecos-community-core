package ru.citeck.ecos.action.node;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Pavel Simonov
 */
public class CreateNodeAction extends NodeActionDefinition {

    private static final String CREATE_NODE_ACTION = "CREATE_NODE";
    private static final String PROP_NODE_TYPE = "nodeType";
    private static final String PROP_DESTINATION = "destination";
    private static final String PROP_DESTINATION_ASSOC = "destinationAssoc";
    private static final String PROP_FORM_ID = "formId";

    public CreateNodeAction() {
        setProperty(PROP_NODE_TYPE, null);
        setProperty(PROP_DESTINATION, null);
        setProperty(PROP_DESTINATION_ASSOC, "cm:contains");
    }

    public String getNodeType() {
        return getProperty(PROP_NODE_TYPE);
    }

    public void setNodeType(String nodeType) {
        setProperty(PROP_NODE_TYPE, nodeType);
    }

    public NodeRef getDestination() {
        String destination = getProperty(PROP_DESTINATION);
        return destination != null ? new NodeRef(destination) : null;
    }

    public void setDestination(NodeRef destination) {
        setProperty(PROP_DESTINATION, destination.toString());
    }

    public void setDestinationAssoc(String assocName) {
        setProperty(PROP_DESTINATION_ASSOC, assocName);
    }

    public String getDestinationAssoc() {
        return getProperty(PROP_DESTINATION_ASSOC);
    }

    public void setFormId(String formId) {
        setProperty(PROP_FORM_ID, formId);
    }

    public String getFormId() {
        return getProperty(PROP_FORM_ID);
    }

    @Override
    protected String getActionType() {
        return CREATE_NODE_ACTION;
    }
}
