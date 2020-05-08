package ru.citeck.ecos.icase.activity.service.eproc.listeners.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityDelegate;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityListenerManager;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.OnEventListener;
import ru.citeck.ecos.records2.RecordRef;

import javax.annotation.PostConstruct;

@Component
public class CaseActivityEventListener implements OnEventListener {

    private EProcActivityService eprocActivityService;
    private EProcCaseActivityDelegate caseActivityDelegate;
    private EProcCaseActivityListenerManager manager;

    @Autowired
    public CaseActivityEventListener(EProcActivityService eprocActivityService,
                                     EProcCaseActivityDelegate caseActivityDelegate,
                                     EProcCaseActivityListenerManager manager) {
        this.eprocActivityService = eprocActivityService;
        this.caseActivityDelegate = caseActivityDelegate;
        this.manager = manager;
    }

    @PostConstruct
    public void init() {
        manager.subscribeOnEvent(this);
    }

    @Override
    public void onEvent(EventRef eventRef) {
        SentryDefinition sentryDef = eprocActivityService.getSentryDefinition(eventRef);

        ActivityRef activityRef = getActivityRefForSentry(eventRef.getProcessId(), sentryDef);
        ActivityTransitionDefinition transitionDefinition = sentryDef
                .getParentTriggerDefinition().getParentActivityTransitionDefinition();

        caseActivityDelegate.doTransition(activityRef, transitionDefinition);
    }

    private ActivityRef getActivityRefForSentry(RecordRef caseRef, SentryDefinition sentryDef) {
        ActivityDefinition activityDefinition = sentryDef.getParentTriggerDefinition()
                .getParentActivityTransitionDefinition()
                .getParentActivityDefinition();

        return ActivityRef.of(CaseServiceType.EPROC, caseRef, activityDefinition.getId());
    }

}
