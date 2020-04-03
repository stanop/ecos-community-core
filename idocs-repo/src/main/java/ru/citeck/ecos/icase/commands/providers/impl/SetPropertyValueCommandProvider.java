package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.commands.CaseCommandsService;
import ru.citeck.ecos.icase.commands.dto.SetPropertyValueCommand;
import ru.citeck.ecos.icase.commands.executors.SetPropertyValueCommandExecutor;
import ru.citeck.ecos.icase.commands.providers.AlfEprocCaseCommandsProvider;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.io.Serializable;

@Component
public class SetPropertyValueCommandProvider extends AlfEprocCaseCommandsProvider {

    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public SetPropertyValueCommandProvider(CaseCommandsService caseCommandsService,
                                           AlfActivityUtils alfActivityUtils,
                                           NodeService nodeService) {
        super(caseCommandsService);
        this.alfActivityUtils = alfActivityUtils;
        this.nodeService = nodeService;
    }

    @Override
    public String getType() {
        return SetPropertyValueCommandExecutor.TYPE;
    }

    @Override
    protected Object provideAlfrescoCommand(ActivityRef activityRef) {
        RecordRef caseRef = activityRef.getProcessId();

        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activityRef);
        QName property = (QName) nodeService.getProperty(activityNodeRef, ActionModel.SetPropertyValue.PROP_PROPERTY);
        Serializable value = nodeService.getProperty(activityNodeRef, ActionModel.SetPropertyValue.PROP_VALUE);

        return new SetPropertyValueCommand(caseRef, property, value);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        //TODO: Add realization for eproc
        throw new UnsupportedOperationException();
    }
}
