package ru.citeck.ecos.action.node;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.apps.app.module.type.type.action.ActionDto;

import java.util.List;
import java.util.Map;

public interface NodeActionsService {

    List<Map<String, String>> getNodeActionsRaw(NodeRef nodeRef);

    List<ActionDto> getNodeActions(NodeRef nodeRef);

    void clearCache(NodeRef nodeRef);

    void addActionProvider(NodeActionsProvider actionsProvider);

}
