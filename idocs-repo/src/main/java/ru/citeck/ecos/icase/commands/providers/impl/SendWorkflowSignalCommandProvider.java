package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.icase.commands.CaseCommandsService;
import ru.citeck.ecos.icase.commands.dto.SendWorkflowSignalCommand;
import ru.citeck.ecos.icase.commands.executors.SendWorkflowSignalCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.AlfActivityUtils;

@Component
public class SendWorkflowSignalCommandProvider extends AlfEprocCaseCommandsProvider {

    private EProcActivityService eprocActivityService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public SendWorkflowSignalCommandProvider(CaseCommandsService caseCommandsService,
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
        return SendWorkflowSignalCommandExecutor.TYPE;
    }

    @Override
    protected Object provideAlfrescoCommand(ActivityRef activityRef) {
        RecordRef caseRef = activityRef.getProcessId();

        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        String signalName = (String) nodeService.getProperty(activityNodeRef,
                ActionModel.SendWorkflowSignal.PROP_SIGNAL_NAME);

        return new SendWorkflowSignalCommand(caseRef, signalName);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);

        RecordRef caseRef = activityRef.getProcessId();
        String signal = EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.ACTION_SEND_WORKFLOW_SIGNAL_NAME);

        return new SendWorkflowSignalCommand(caseRef, signal);
    }
}
