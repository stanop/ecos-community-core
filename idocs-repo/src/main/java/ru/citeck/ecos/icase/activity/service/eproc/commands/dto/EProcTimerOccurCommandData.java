package ru.citeck.ecos.icase.activity.service.eproc.commands.dto;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.commands.executors.EProcTimerOccurExecutor;

@Data
@CommandType(EProcTimerOccurExecutor.TYPE)
public class EProcTimerOccurCommandData {
    private String timerId;
    private ActivityRef activityRef;
}
