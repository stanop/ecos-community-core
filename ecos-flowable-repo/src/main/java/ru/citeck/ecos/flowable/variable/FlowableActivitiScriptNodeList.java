package ru.citeck.ecos.flowable.variable;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.List;

public class FlowableActivitiScriptNodeList extends ArrayList<FlowableActivitiScriptNode> {

    private static final long serialVersionUID = 8314466309493919187L;

    public List<NodeRef> getNodeReferences() {
        // Extract all node references
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        for (FlowableActivitiScriptNode scriptNode : this) {
            nodeRefs.add(scriptNode.getNodeRef());
        }
        return nodeRefs;
    }

    @Override
    public int size() {
        return super.size();
    }
}
