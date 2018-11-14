package ru.citeck.ecos.flowable.variable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.flowable.engine.common.api.FlowableException;
import org.flowable.variable.api.types.ValueFields;
import org.flowable.variable.service.impl.types.SerializableType;

import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class FlowableScriptNodeListVariableType extends SerializableType {

    private static final String TYPE = "flwAlfrescoScriptNodeList";

    private ServiceRegistry serviceRegistry;

    @Override
    public String getTypeName() {
        return TYPE;
    }

    @Override
    public boolean isCachable() {
        // The FlowableScriptNodeList can be cached since it uses the serviceRegistry internally
        // for resolving actual values.
        return true;
    }

    @Override
    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }
        return FlowableScriptNodeList.class.isAssignableFrom(value.getClass());
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        if (value != null) {
            if (!(value instanceof FlowableScriptNodeList)) {
                throw new FlowableException("Passed value is not an instance of FlowableScriptNodeList, cannot set " +
                        "variable value.");
            }

            // Extract all node references
            List<NodeRef> nodeRefs = ((FlowableScriptNodeList) value).getNodeReferences();
            // Save the list as a serializable
            super.setValue(nodeRefs, valueFields);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getValue(ValueFields valueFields) {
        Object serializable = super.getValue(valueFields);
        if (serializable == null) {
            return null;
        }

        if (!(serializable instanceof List<?>)) {
            throw new FlowableException("Serializable stored in variable is not instance of List<NodeRef>, " +
                    "cannot get value.");
        }

        FlowableScriptNodeList scriptNodes = new FlowableScriptNodeList();
        // Wrap all node references in an FlowableScriptNodeList
        List<NodeRef> nodeRefs = (List<NodeRef>) serializable;
        for (NodeRef ref : nodeRefs) {
            FlowableScriptNode node = new FlowableScriptNode(ref, serviceRegistry);
            scriptNodes.add(node);
        }
        return scriptNodes;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
