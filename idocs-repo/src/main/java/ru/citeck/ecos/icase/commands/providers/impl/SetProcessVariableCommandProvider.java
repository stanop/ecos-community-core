package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.commands.CaseCommandsService;
import ru.citeck.ecos.icase.commands.dto.SetProcessVariableCommand;
import ru.citeck.ecos.icase.commands.executors.SetProcessVariableCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.utils.AlfActivityUtils;

@Component
public class SetProcessVariableCommandProvider extends AlfEprocCaseCommandsProvider {

    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public SetProcessVariableCommandProvider(CaseCommandsService caseCommandsService,
                                             AlfActivityUtils alfActivityUtils,
                                             NodeService nodeService) {
        super(caseCommandsService);
        this.alfActivityUtils = alfActivityUtils;
        this.nodeService = nodeService;
    }

    @Override
    public String getType() {
        return SetProcessVariableCommandExecutor.TYPE;
    }

    @Override
    protected Object provideAlfrescoCommand(ActivityRef activityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        String varName = (String) nodeService.getProperty(activityNodeRef, ActionModel.SetProcessVariable.PROP_VARIABLE);
        String varValue = (String) nodeService.getProperty(activityNodeRef, ActionModel.SetProcessVariable.PROP_VALUE);
        return new SetProcessVariableCommand(varName, varValue);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        //TODO: Add realization for eproc
        throw new UnsupportedOperationException();
    }
}
