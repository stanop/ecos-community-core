package ru.citeck.ecos.icase.commands.executors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.icase.commands.dto.FailCommand;

import javax.annotation.PostConstruct;

@Component
public class FailCommandExecutor implements CommandExecutor<FailCommand> {

    public static final String TYPE = "fail";

    private CommandsService commandsService;

    @Autowired
    public FailCommandExecutor(CommandsService commandsService) {
        this.commandsService = commandsService;
    }

    @PostConstruct
    public void init() {
        commandsService.addExecutor(this);
    }

    @Nullable
    @Override
    public Object execute(FailCommand failCommand) {
        throw new AlfrescoRuntimeException(failCommand.getFailMessage());
    }
}
