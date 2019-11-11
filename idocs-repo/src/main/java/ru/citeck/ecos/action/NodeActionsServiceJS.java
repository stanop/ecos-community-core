package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.action.node.NodeActionsService;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import java.util.List;
import java.util.Map;

/**
 * @author deathNC on 30.04.2016.
 */
public class NodeActionsServiceJS extends AlfrescoScopableProcessorExtension {

    private NodeActionsService nodeActionsService;

    public List<Map<String, String>> getActions(String nodeRef) {
        NodeRef ref = new NodeRef(nodeRef);
        return nodeActionsService.getNodeActionsRaw(ref);
    }

    public void setNodeActionsService(NodeActionsService nodeActionsService) {
        this.nodeActionsService = nodeActionsService;
    }

}
