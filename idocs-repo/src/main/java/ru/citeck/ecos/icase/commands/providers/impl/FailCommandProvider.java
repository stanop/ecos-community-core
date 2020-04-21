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
import ru.citeck.ecos.icase.commands.dto.FailCommand;
import ru.citeck.ecos.icase.commands.executors.FailCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.utils.AlfActivityUtils;

@Component
public class FailCommandProvider extends AlfEprocCaseCommandsProvider {

    private EProcActivityService eprocActivityService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public FailCommandProvider(CaseCommandsService caseCommandsService,
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
        return FailCommandExecutor.TYPE;
    }

    @Override
    protected Object provideAlfrescoCommand(ActivityRef activityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        String msg = (String) nodeService.getProperty(activityNodeRef, ActionModel.Fail.PROP_MESSAGE);
        return new FailCommand(msg);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        ActivityDefinition definition = eprocActivityService.getActivityDefinition(activityRef);

        String msg = EProcUtils.getDefAttribute(definition, CmmnDefinitionConstants.ACTION_FAIL_MESSAGE);

        return new FailCommand(msg);
    }
}
