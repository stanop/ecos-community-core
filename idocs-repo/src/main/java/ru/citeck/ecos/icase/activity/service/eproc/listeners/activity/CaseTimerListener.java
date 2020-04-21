package ru.citeck.ecos.icase.activity.service.eproc.listeners.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityDefinition;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityListenerManager;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.BeforeStartedActivityListener;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.BeforeStoppedActivityListener;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.OnResetActivityListener;
import ru.citeck.ecos.icase.activity.service.eproc.timer.EProcCaseTimerService;

import javax.annotation.PostConstruct;

@Component
public class CaseTimerListener implements
        BeforeStartedActivityListener,
        BeforeStoppedActivityListener,
        OnResetActivityListener {

    private EProcCaseActivityListenerManager manager;
    private EProcCaseTimerService eprocCaseTimerService;
    private EProcActivityService eprocActivityService;

    @Autowired
    public CaseTimerListener(EProcCaseActivityListenerManager manager,
                             EProcCaseTimerService eprocCaseTimerService,
                             EProcActivityService eprocActivityService) {
        this.manager = manager;
        this.eprocCaseTimerService = eprocCaseTimerService;
        this.eprocActivityService = eprocActivityService;
    }

    @PostConstruct
    public void init() {
        manager.subscribeBeforeStarted(this);
        manager.subscribeBeforeStopped(this);
        manager.subscribeOnReset(this);
    }

    @Override
    public void beforeStartedActivity(ActivityRef activityRef) {
        if (isTimer(activityRef)) {
            eprocCaseTimerService.scheduleTimer(activityRef);
        }
    }

    @Override
    public void beforeStoppedActivity(ActivityRef activityRef) {
        if (isTimer(activityRef)) {
            eprocCaseTimerService.cancelTimer(activityRef);
        }
    }

    @Override
    public void onResetActivity(ActivityRef activityRef) {
        if (isTimer(activityRef)) {
            eprocCaseTimerService.cancelTimer(activityRef);
        }
    }

    private boolean isTimer(ActivityRef activityRef) {
        ActivityDefinition activityDefinition = eprocActivityService.getActivityDefinition(activityRef);
        return EProcUtils.isTimer(activityDefinition);
    }

}
