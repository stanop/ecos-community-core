package ru.citeck.ecos.commands.timer.dto.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;

@Data
@CommandType("cancel-timer")
public class CancelTimerCommand {
    private String timerId;
}