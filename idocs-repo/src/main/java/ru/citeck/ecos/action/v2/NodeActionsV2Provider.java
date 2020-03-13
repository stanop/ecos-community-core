package ru.citeck.ecos.action.v2;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.action.ActionModule;

import java.util.List;

public interface NodeActionsV2Provider {

    List<ActionModule> getActions(NodeRef nodeRef);

    String getScope();
}
