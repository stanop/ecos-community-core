package ru.citeck.ecos.icase.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SetProcessVariableCommandExecutor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType(SetProcessVariableCommandExecutor.TYPE)
public class SetProcessVariableCommand {
    private String variableName;
    private String variableValue;
}
