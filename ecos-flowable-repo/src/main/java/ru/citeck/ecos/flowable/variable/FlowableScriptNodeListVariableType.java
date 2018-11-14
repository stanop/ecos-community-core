package ru.citeck.ecos.flowable.variable;

import org.alfresco.repo.workflow.activiti.ActivitiScriptNodeList;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.flowable.engine.common.api.FlowableException;
import org.flowable.variable.api.types.ValueFields;
import org.flowable.variable.service.impl.types.SerializableType;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class FlowableScriptNodeListVariableType extends SerializableType {

    public static final String TYPE = "flwAlfrescoScriptNodeList";

    private ServiceRegistry serviceRegistry;

    @Override
    public String getTypeName() {
        return TYPE;
    }

    @Override
    public boolean isCachable() {
        // The FlowableActivitiScriptNodeList can be cached since it uses the serviceRegistry internally
        // for resolving actual values.
        return true;
    }

    @Override
    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }
        return FlowableActivitiScriptNodeList.class.isAssignableFrom(value.getClass());
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        if (value != null) {
            if (!(value instanceof FlowableActivitiScriptNodeList)) {
                throw new FlowableException("Passed value is not an instance of FlowableActivitiScriptNodeList, cannot set variable value.");
            }

            // Extract all node references
            List<NodeRef> nodeRefs = ((FlowableActivitiScriptNodeList) value).getNodeReferences();
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
            throw new FlowableException("Serializable stored in variable is not instance of List<NodeRef>, cannot get value.");
        }

        FlowableActivitiScriptNodeList scriptNodes = new FlowableActivitiScriptNodeList();
        // Wrap all node references in an FlowableActivitiScriptNodeList
        List<NodeRef> nodeRefs = (List<NodeRef>) serializable;
        for (NodeRef ref : nodeRefs) {

            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();

            FlowableActivitiScriptNode node;
            try {
                node = new FlowableActivitiScriptNode(ref, serviceRegistry, scope);
            } finally {
                Context.exit();
            }

            scriptNodes.add(node);


        }
        return scriptNodes;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
