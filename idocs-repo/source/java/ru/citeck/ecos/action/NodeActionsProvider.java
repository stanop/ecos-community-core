package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * @author deathNC on 30.04.2016.
 */
public abstract class NodeActionsProvider {

    public NodeActionsProvider() {
    }

    public void setNodeActionsService(NodeActionsService nodeActionsService) {
        nodeActionsService.addActionProvider(this);

    }

    public abstract List<NodeActionDefinition> getNodeActions(NodeRef nodeRef);

}
