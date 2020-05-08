package ru.citeck.ecos.icase.activity.service.eproc.commands.executors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.icase.activity.service.eproc.commands.dto.EProcTimerOccurCommandData;
import ru.citeck.ecos.icase.activity.service.eproc.timer.EProcCaseTimerService;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class EProcTimerOccurExecutor implements CommandExecutor<EProcTimerOccurCommandData> {

    public static final String TYPE = "eproc-timer-occur";

    private CommandsService commandsService;
    private EProcCaseTimerService eprocCaseTimerService;

    @Autowired
    public EProcTimerOccurExecutor(CommandsService commandsService,
                                   EProcCaseTimerService eprocCaseTimerService) {
        this.commandsService = commandsService;
        this.eprocCaseTimerService = eprocCaseTimerService;
    }

    @PostConstruct
    public void init() {
        commandsService.addExecutor(this);
    }

    @Override
    public Object execute(EProcTimerOccurCommandData data) {
        if (data == null) {
            throw new IllegalArgumentException("Received empty command");
        }

        eprocCaseTimerService.timerOccurred(data);
        return null;
    }
}
