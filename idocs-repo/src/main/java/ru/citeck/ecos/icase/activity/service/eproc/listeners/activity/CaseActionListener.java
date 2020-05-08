package ru.citeck.ecos.icase.activity.service.eproc.listeners.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityDefinition;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityListenerManager;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.BeforeStartedActivityListener;
import ru.citeck.ecos.icase.commands.CaseCommandsService;

import javax.annotation.PostConstruct;

@Component
public class CaseActionListener implements BeforeStartedActivityListener {

    private EProcCaseActivityListenerManager manager;
    private EProcActivityService eprocActivityService;
    private CaseCommandsService caseCommandsService;
    private CaseActivityService caseActivityService;

    @Autowired
    public CaseActionListener(EProcCaseActivityListenerManager manager,
                              EProcActivityService eprocActivityService,
                              CaseCommandsService caseCommandsService,
                              CaseActivityService caseActivityService) {
        this.manager = manager;
        this.eprocActivityService = eprocActivityService;
        this.caseCommandsService = caseCommandsService;
        this.caseActivityService = caseActivityService;
    }

    @PostConstruct
    public void init() {
        manager.subscribeBeforeStarted(this);
    }

    @Override
    public void beforeStartedActivity(ActivityRef activityRef) {
        ActivityDefinition definition = eprocActivityService.getActivityDefinition(activityRef);
        if (!EProcUtils.isAction(definition)) {
            return;
        }

        caseCommandsService.executeCaseAction(activityRef);
        caseActivityService.stopActivity(activityRef);
    }

}
