package ru.citeck.ecos.flowable.converters;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.AbstractWorkflowNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Flowable node converter
 */
public class FlowableNodeConverter extends AbstractWorkflowNodeConverter {

    /**
     * Service registry
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * Constructor
     * @param serviceRegistry Service registry
     */
    public FlowableNodeConverter(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Convert node
     * @param node Node
     * @return Node
     */
    @Override
    public Object convertNode(NodeRef node) {
        return node;
    }

    /**
     * Convert nodes
     * @param values Nodes collection
     * @return Nodes list
     */
    @Override
    public List<? extends Object> convertNodes(Collection<NodeRef> values) {
        return new ArrayList<>(values);
    }

    /**
     * Convert to node
     * @param toConvert Script node
     * @return Node reference
     */
    @Override
    public NodeRef convertToNode(Object toConvert) {
        return ((ScriptNode)toConvert).getNodeRef();
    }

    /**
     * Is supported
     */
    @Override
    public boolean isSupported(Object object) {
        return false;
    }
}