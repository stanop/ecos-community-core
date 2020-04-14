package ru.citeck.ecos.icase.activity.service.eproc.listeners.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcCaseActivityListenerManager;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.OnEventListener;
import ru.citeck.ecos.records2.RecordRef;

import javax.annotation.PostConstruct;

@Component
public class CaseActivityEventListener implements OnEventListener {

    private EProcActivityService eprocActivityService;
    private CaseActivityService caseActivityService;
    private EProcCaseActivityListenerManager manager;

    @Autowired
    public CaseActivityEventListener(EProcActivityService eprocActivityService,
                                     CaseActivityService caseActivityService,
                                     EProcCaseActivityListenerManager manager) {
        this.eprocActivityService = eprocActivityService;
        this.caseActivityService = caseActivityService;
        this.manager = manager;
    }

    @PostConstruct
    public void init() {
        manager.subscribeOnEvent(this);
    }

    @Override
    public void onEvent(EventRef eventRef) {
        SentryDefinition sentryDef = eprocActivityService.getSentryDefinition(eventRef);
        ActivityTransitionDefinition transitionDefinition = sentryDef
                .getParentTriggerDefinition().getParentActivityTransitionDefinition();

        switch (transitionDefinition.getToState()) {
            case STARTED:
                caseActivityService.startActivity(getActivityRefForSentry(eventRef.getProcessId(), sentryDef));
                break;
            case COMPLETED:
                caseActivityService.stopActivity(getActivityRefForSentry(eventRef.getProcessId(), sentryDef));
                break;
        }
    }

    private ActivityRef getActivityRefForSentry(RecordRef caseRef, SentryDefinition sentryDef) {
        ActivityDefinition activityDefinition = sentryDef.getParentTriggerDefinition()
                .getParentActivityTransitionDefinition()
                .getParentActivityDefinition();

        return ActivityRef.of(CaseServiceType.EPROC, caseRef, activityDefinition.getId());
    }

}
