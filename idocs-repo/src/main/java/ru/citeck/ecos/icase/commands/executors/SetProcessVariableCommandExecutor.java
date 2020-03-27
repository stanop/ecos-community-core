package ru.citeck.ecos.icase.commands.executors;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.icase.commands.dto.SetProcessVariableCommand;
import ru.citeck.ecos.lifecycle.LifeCycleServiceImpl;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Component
public class SetProcessVariableCommandExecutor implements CommandExecutor<SetProcessVariableCommand> {

    public static final String TYPE = "set-process-variable";

    private CommandsService commandsService;

    @Autowired
    public SetProcessVariableCommandExecutor(CommandsService commandsService) {
        this.commandsService = commandsService;
    }

    @PostConstruct
    public void init() {
        commandsService.addExecutor(this);
    }

    @Nullable
    @Override
    public Object execute(SetProcessVariableCommand command) {
        if (StringUtils.isBlank(command.getVariableName())) {
            log.error("Variable name is mandatory string");
            return null;
        }

        if (!AlfrescoTransactionSupport.isActualTransactionActive()) {
            log.error("Actual transaction is not active. Can't set variable " + command.getVariableName());
            return null;
        }

        Map<String, Object> processVariables = AlfrescoTransactionSupport.getResource(LifeCycleServiceImpl.PROCESS_VARS);
        if (processVariables == null) {
            log.error("Process variables are undefined. Make sure you call this action executor in lifecycle context.");
            return null;
        }

        processVariables.put(command.getVariableName(), command.getVariableValue());
        return null;
    }
}
