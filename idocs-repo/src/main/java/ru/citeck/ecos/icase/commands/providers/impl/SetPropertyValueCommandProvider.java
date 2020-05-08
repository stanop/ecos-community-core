package ru.citeck.ecos.icase.commands.providers.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityDefinition;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
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

    private EProcActivityService eprocActivityService;
    private NamespaceService namespaceService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public SetPropertyValueCommandProvider(CaseCommandsService caseCommandsService,
                                           EProcActivityService eprocActivityService,
                                           NamespaceService namespaceService,
                                           AlfActivityUtils alfActivityUtils,
                                           NodeService nodeService) {
        super(caseCommandsService);
        this.eprocActivityService = eprocActivityService;
        this.namespaceService = namespaceService;
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
        String property = ((QName) nodeService.getProperty(activityNodeRef, ActionModel.SetPropertyValue.PROP_PROPERTY)).toString();
        Serializable value = nodeService.getProperty(activityNodeRef, ActionModel.SetPropertyValue.PROP_VALUE);

        return new SetPropertyValueCommand(caseRef, property, value);
    }

    @Override
    protected Object provideEprocCommand(ActivityRef activityRef) {
        ActivityDefinition definition = eprocActivityService.getActivityDefinition(activityRef);

        String name = EProcUtils.getDefAttribute(definition, CmmnDefinitionConstants.ACTION_SET_PROPERTY_PROP_NAME);
        String value = EProcUtils.getDefAttribute(definition, CmmnDefinitionConstants.ACTION_SET_PROPERTY_PROP_VALUE);

        return new SetPropertyValueCommand(activityRef.getProcessId(), name, value);
    }
}
