package ru.citeck.ecos.icase.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.FailCommandExecutor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType(FailCommandExecutor.TYPE)
public class FailCommand {
    private String failMessage;
}
