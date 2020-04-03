package ru.citeck.ecos.icase.commands.executors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.icase.commands.dto.SetCaseStatusCommand;
import ru.citeck.ecos.records.RecordsUtils;

import javax.annotation.PostConstruct;

@Component
public class SetCaseStatusCommandExecutor implements CommandExecutor<SetCaseStatusCommand> {

    public static final String TYPE = "set-case-status";

    private CommandsService commandsService;
    private CaseStatusService caseStatusService;

    @Autowired
    public SetCaseStatusCommandExecutor(CommandsService commandsService, CaseStatusService caseStatusService) {
        this.commandsService = commandsService;
        this.caseStatusService = caseStatusService;
    }

    @PostConstruct
    public void init() {
        commandsService.addExecutor(this);
    }

    @Nullable
    @Override
    public Object execute(SetCaseStatusCommand command) {
        NodeRef caseRef = RecordsUtils.toNodeRef(command.getCaseRef());
        caseStatusService.setStatus(caseRef, command.getStatusName());
        return null;
    }
}
