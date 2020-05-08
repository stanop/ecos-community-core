package ru.citeck.ecos.icase.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.ExecuteScriptCommandExecutor;
import ru.citeck.ecos.records2.RecordRef;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType(ExecuteScriptCommandExecutor.TYPE)
public class ExecuteScriptCommand {
    private RecordRef caseRef;
    private String script;
}
