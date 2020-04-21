package ru.citeck.ecos.commands.timer.dto.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;

import java.time.Instant;

@Data
@CommandType("create-timer")
public class CreateTimerCommand {
    private Instant triggerTime;
    private TimerCommandDto command;
}
