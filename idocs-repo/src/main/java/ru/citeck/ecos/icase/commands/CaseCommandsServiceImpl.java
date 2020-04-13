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
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.commands.providers.CaseCommandsProvider;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("caseCommandsService")
public class CaseCommandsServiceImpl implements CaseCommandsService {

    private Map<String, CaseCommandsProvider> typeToProviderMap = new ConcurrentHashMap<>();

    @Autowired
    private CommandsService commandsService;

    @Autowired
    private AlfActivityUtils alfActivityUtils;

    @Autowired
    private NodeService nodeService;

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
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            CommandError error = commandResult.getErrors().get(0);
            throw new RuntimeException("Exception while processing action '" + activityRef +
                "', exceptionMessage='" + error.getMessage() +
                "', exceptionType='" + error.getType() + "'. " +
                "StackTrace of root exception may be fount in logs");
        }
    }

    private String getType(ActivityRef actionActivityRef) {
        if (actionActivityRef.getCaseServiceType() == CaseServiceType.ALFRESCO) {
            return getAlfrescoType(actionActivityRef);
        } else {
            //TODO: Add realization for eproc
            throw new UnsupportedOperationException();
        }
    }

    private String getAlfrescoType(ActivityRef actionActivityRef) {
        NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(actionActivityRef);
        QName type = nodeService.getType(activityNodeRef);
        return type.getLocalName();
    }

    @Override
    public void register(CaseCommandsProvider caseCommandsProvider) {
        this.typeToProviderMap.put(caseCommandsProvider.getType(), caseCommandsProvider);
        log.info("Registered case commands provider for type " + caseCommandsProvider.getType() +
            ", class " + caseCommandsProvider.getClass().getName());
    }
}
