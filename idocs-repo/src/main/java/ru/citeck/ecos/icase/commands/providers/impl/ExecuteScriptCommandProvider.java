package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityDefinition;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.icase.commands.CaseCommandsService;
import ru.citeck.ecos.icase.commands.dto.ExecuteScriptCommand;
import ru.citeck.ecos.icase.commands.executors.ExecuteScriptCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.AlfActivityUtils;

@Component
public class ExecuteScriptCommandProvider extends AlfEprocCaseCommandsProvider {

    private EProcActivityService eprocActivityService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public ExecuteScriptCommandProvider(CaseCommandsService caseCommandsService,
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
        return ExecuteScriptCommandExecutor.TYPE;
    }

    @Override
    protected Object provideAlfrescoCommand(ActivityRef activityRef) {
        RecordRef caseRef = activityRef.getProcessId();

        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        String script = (String) nodeService.getProperty(activityNodeRef, ActionModel.ExecuteScript.PROP_SCRIPT);

        return new ExecuteScriptCommand(caseRef, script);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        ActivityDefinition definition = eprocActivityService.getActivityDefinition(activityRef);

        RecordRef caseRef = activityRef.getProcessId();

        String script = EProcUtils.getDefAttribute(definition, CmmnDefinitionConstants.ACTION_SCRIPT);

        return new ExecuteScriptCommand(caseRef, script);
    }
}
