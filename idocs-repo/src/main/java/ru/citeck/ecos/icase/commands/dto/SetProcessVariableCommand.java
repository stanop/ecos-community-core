package ru.citeck.ecos.icase.commands.dto;

import lombok.Data;
import lombok.NonNull;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SetProcessVariableCommandExecutor;

@Data
@CommandType(SetProcessVariableCommandExecutor.TYPE)
public class SetProcessVariableCommand {
    @NonNull
    private final String variableName;
    private final String variableValue;
}
