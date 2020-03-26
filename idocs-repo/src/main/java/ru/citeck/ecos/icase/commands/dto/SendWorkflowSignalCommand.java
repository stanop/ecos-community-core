package ru.citeck.ecos.icase.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SendWorkflowSignalCommandExecutor;
import ru.citeck.ecos.records2.RecordRef;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType(SendWorkflowSignalCommandExecutor.TYPE)
public class SendWorkflowSignalCommand {
    private RecordRef caseRef;
    private String signalName;
}
