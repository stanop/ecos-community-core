package ru.citeck.ecos.icase.commands.dto;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.FailCommandExecutor;

@Data
@CommandType(FailCommandExecutor.TYPE)
public class FailCommand {
    private final String failMessage;
}
