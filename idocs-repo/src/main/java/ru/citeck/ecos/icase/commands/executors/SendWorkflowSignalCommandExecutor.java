package ru.citeck.ecos.icase.commands.executors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.icase.commands.dto.SendWorkflowSignalCommand;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.workflow.EcosWorkflowService;

import javax.annotation.PostConstruct;

@Component
public class SendWorkflowSignalCommandExecutor implements CommandExecutor<SendWorkflowSignalCommand> {

    public static final String TYPE = "send-workflow-signal";

    private CommandsService commandsService;
    private EcosWorkflowService ecosWorkflowService;

    @Autowired
    public SendWorkflowSignalCommandExecutor(CommandsService commandsService, EcosWorkflowService ecosWorkflowService) {
        this.commandsService = commandsService;
        this.ecosWorkflowService = ecosWorkflowService;
    }

    @PostConstruct
    public void init() {
        commandsService.addExecutor(this);
    }

    @Nullable
    @Override
    public Object execute(SendWorkflowSignalCommand command) {
        NodeRef caseRef = RecordsUtils.toNodeRef(command.getCaseRef());
        ecosWorkflowService.sendSignal(caseRef, command.getSignalName());
        return null;
    }
}
