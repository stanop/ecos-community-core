package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.commands.CaseCommandsService;
import ru.citeck.ecos.icase.commands.dto.SetCaseStatusCommand;
import ru.citeck.ecos.icase.commands.executors.SetCaseStatusCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.RepoUtils;

@Component
public class SetCaseStatusCommandProvider extends AlfEprocCaseCommandsProvider {

    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public SetCaseStatusCommandProvider(CaseCommandsService caseCommandsService,
                                        AlfActivityUtils alfActivityUtils,
                                        NodeService nodeService) {
        super(caseCommandsService);
        this.alfActivityUtils = alfActivityUtils;
        this.nodeService = nodeService;
    }

    @Override
    public String getType() {
        return SetCaseStatusCommandExecutor.TYPE;
    }

    @Override
    protected Object provideAlfrescoCommand(ActivityRef activityRef) {
        RecordRef caseRef = activityRef.getProcessId();

        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        NodeRef statusRef = RepoUtils.getFirstTargetAssoc(activityNodeRef, ActionModel.SetCaseStatus.ASSOC_STATUS, nodeService);
        String statusName = (String) nodeService.getProperty(statusRef, ContentModel.PROP_NAME);

        return new SetCaseStatusCommand(caseRef, statusName);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        //TODO: Add realization for eproc
        throw new UnsupportedOperationException();
    }

}
