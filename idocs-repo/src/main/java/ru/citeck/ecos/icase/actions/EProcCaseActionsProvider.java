package ru.citeck.ecos.icase.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.node.NodeActionDefinition;

import java.util.List;

@Service
public class EProcCaseActionsProvider implements CaseActionsProvider {

    @Override
    public List<NodeActionDefinition> getCaseActions(NodeRef caseRef) {
        //TODO: Add realization for eproc
        throw new UnsupportedOperationException();
    }
}
