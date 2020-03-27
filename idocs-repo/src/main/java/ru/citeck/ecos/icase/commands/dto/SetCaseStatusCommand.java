package ru.citeck.ecos.icase.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SetCaseStatusCommandExecutor;
import ru.citeck.ecos.records2.RecordRef;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType(SetCaseStatusCommandExecutor.TYPE)
public class SetCaseStatusCommand {
    private RecordRef caseRef;
    private String statusName;
}
