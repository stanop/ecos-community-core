package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.List;

/**
 * @author deathNC on 30.04.2016.
 */
public abstract class NodeActionsProvider {

    private NodeService nodeService;

    public NodeActionsProvider() {
    }

    public void setNodeActionsService(NodeActionsService nodeActionsService) {
        nodeActionsService.addActionProvider(this);

    }

    public abstract List<NodeActionDefinition> getNodeActions(NodeRef nodeRef);

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
