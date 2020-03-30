package ru.citeck.ecos.icase.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.action.node.NodeActionDefinition;

import java.util.List;

public interface CaseActionsProvider {

    List<NodeActionDefinition> getCaseActions(NodeRef caseRef);

}
