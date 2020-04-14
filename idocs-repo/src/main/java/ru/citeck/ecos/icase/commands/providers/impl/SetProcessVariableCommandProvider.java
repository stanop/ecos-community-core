package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.icase.commands.CaseCommandsService;
import ru.citeck.ecos.icase.commands.dto.SetProcessVariableCommand;
import ru.citeck.ecos.icase.commands.executors.SetProcessVariableCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.utils.AlfActivityUtils;

@Component
public class SetProcessVariableCommandProvider extends AlfEprocCaseCommandsProvider {

    private EProcActivityService eprocActivityService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public SetProcessVariableCommandProvider(CaseCommandsService caseCommandsService,
                                             EProcActivityService eprocActivityService,
                                             AlfActivityUtils alfActivityUtils,
                                             NodeService nodeService) {
        super(caseCommandsService);
        this.eprocActivityService = eprocActivityService;
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
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);

        String name = EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.ACTION_SET_PROCESS_VAR_NAME);
        String value = EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.ACTION_SET_PROCESS_VAR_VALUE);

        return new SetProcessVariableCommand(name, value);
    }
}
