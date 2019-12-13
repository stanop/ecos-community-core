package ru.citeck.ecos.flowable.variable;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Makarskiy
 */
public class FlowableScriptNodeList extends ArrayList<FlowableScriptNode> {

    private static final long serialVersionUID = 8314466309493919187L;

    public List<NodeRef> getNodeReferences() {
        // Extract all node references
        List<NodeRef> nodeRefs = new ArrayList<>();
        for (FlowableScriptNode scriptNode : this) {
            nodeRefs.add(scriptNode.getNodeRef());
        }
        return nodeRefs;
    }
}
