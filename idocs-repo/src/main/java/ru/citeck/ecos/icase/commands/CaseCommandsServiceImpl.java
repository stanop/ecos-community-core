package ru.citeck.ecos.icase.commands;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.commands.dto.CommandError;
import ru.citeck.ecos.commands.dto.CommandResult;
import ru.citeck.ecos.icase.activity.dto.ActivityDefinition;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.icase.commands.providers.CaseCommandsProvider;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("caseCommandsService")
public class CaseCommandsServiceImpl implements CaseCommandsService {

    private Map<String, CaseCommandsProvider> typeToProviderMap = new ConcurrentHashMap<>();

    private EProcActivityService eprocActivityService;
    private CommandsService commandsService;
    private AlfActivityUtils alfActivityUtils;
    private NodeService nodeService;

    @Autowired
    public CaseCommandsServiceImpl(EProcActivityService eprocActivityService,
                                   CommandsService commandsService,
                                   AlfActivityUtils alfActivityUtils,
                                   NodeService nodeService) {
        this.eprocActivityService = eprocActivityService;
        this.commandsService = commandsService;
        this.alfActivityUtils = alfActivityUtils;
        this.nodeService = nodeService;
    }

    @Override
    public void executeCaseAction(ActivityRef activityRef) {
        String type = getType(activityRef);
        CaseCommandsProvider caseCommandsProvider = typeToProviderMap.get(type);
        if (caseCommandsProvider == null) {
            throw new IllegalStateException("For activityRef=" + activityRef +
                    ", type=" + type + " provider was not found");
        }

        Object commandDto = caseCommandsProvider.provideCommandDto(activityRef);
        CommandResult commandResult = commandsService.executeSync(commandDto);
        if (commandResult.getPrimaryError() != null) {
            log.error("Exception while processing action '" + activityRef + "'");
        }
        commandResult.throwPrimaryErrorIfNotNull();
    }

    private String getType(ActivityRef actionActivityRef) {
        if (actionActivityRef.getCaseServiceType() == CaseServiceType.ALFRESCO) {
            return getAlfrescoType(actionActivityRef);
        } else {
            return getEProcActionType(actionActivityRef);
        }
    }

    private String getAlfrescoType(ActivityRef actionActivityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(actionActivityRef);
        QName type = nodeService.getType(activityNodeRef);
        return type.getLocalName();
    }

    private String getEProcActionType(ActivityRef actionActivityRef) {
        ActivityDefinition definition = eprocActivityService.getActivityDefinition(actionActivityRef);
        return EProcUtils.getDefAttribute(definition, CmmnDefinitionConstants.ACTION_TYPE);
    }

    @Override
    public void register(CaseCommandsProvider caseCommandsProvider) {
        this.typeToProviderMap.put(caseCommandsProvider.getType(), caseCommandsProvider);
        log.info("Registered case commands provider for type " + caseCommandsProvider.getType() +
                ", class " + caseCommandsProvider.getClass().getName());
    }
}
