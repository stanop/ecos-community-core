package ru.citeck.ecos.icase.activity.service.eproc.timer;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.commands.dto.EProcTimerOccurCommandData;

public interface EProcCaseTimerService {

    void scheduleTimer(ActivityRef activityRef);

    void timerOccurred(EProcTimerOccurCommandData data);

    void cancelTimer(ActivityRef activityRef);

}
