package ru.citeck.ecos.icase.commands.dto;

import lombok.Data;
import lombok.NonNull;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SetCaseStatusCommandExecutor;
import ru.citeck.ecos.records2.RecordRef;

@Data
@CommandType(SetCaseStatusCommandExecutor.TYPE)
public class SetCaseStatusCommand {
    @NonNull
    private final RecordRef caseRef;
    @NonNull
    private final String statusName;
}
