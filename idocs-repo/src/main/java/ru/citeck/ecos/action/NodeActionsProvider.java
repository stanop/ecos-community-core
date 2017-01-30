package ru.citeck.ecos.action;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.action.node.NodeActionDefinition;

import java.util.List;

/**
 * @author deathNC on 30.04.2016.
 */
public abstract class NodeActionsProvider {

    protected NodeService nodeService;
    protected DictionaryService dictionaryService;

    public NodeActionsProvider() {
    }

    public void setNodeActionsService(NodeActionsService nodeActionsService) {
        nodeActionsService.addActionProvider(this);

    }

    public abstract List<NodeActionDefinition> getNodeActions(NodeRef nodeRef);

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
