package ru.citeck.ecos.action.node;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.action.ActionModule;

import java.util.List;
import java.util.Map;

public interface NodeActionsService {

    List<Map<String, String>> getNodeActionsRaw(NodeRef nodeRef);

    List<ActionModule> getNodeActions(NodeRef nodeRef);

    void clearCache(NodeRef nodeRef);

    void addActionProvider(NodeActionsProvider actionsProvider);

}
