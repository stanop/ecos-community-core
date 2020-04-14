package ru.citeck.ecos.icase.activity.service.eproc;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.*;

import java.util.Set;
import java.util.TreeSet;

@Component
public class EProcCaseActivityListenerManager {

    private Set<BeforeStartedActivityListener> beforeStartedListeners = new TreeSet<>();
    private Set<OnStartedActivityListener> onStartedListeners = new TreeSet<>();
    private Set<BeforeStoppedActivityListener> beforeStoppedListeners = new TreeSet<>();
    private Set<OnStoppedActivityListener> onStoppedListeners = new TreeSet<>();
    private Set<OnResetActivityListener> onResetListeners = new TreeSet<>();

    private Set<BeforeEventListener> beforeEventListeners = new TreeSet<>();
    private Set<OnEventListener> onEventListeners = new TreeSet<>();


    //////////////////////////
    /* PUBLIC SUBSCRIPTIONS */
    //////////////////////////

    public void subscribeBeforeStarted(BeforeStartedActivityListener listener) {
        this.beforeStartedListeners.add(listener);
    }

    public void subscribeOnStarted(OnStartedActivityListener listener) {
        this.onStartedListeners.add(listener);
    }

    public void subscribeBeforeStopped(BeforeStoppedActivityListener listener) {
        this.beforeStoppedListeners.add(listener);
    }

    public void subscribeOnStopped(OnStoppedActivityListener listener) {
        this.onStoppedListeners.add(listener);
    }

    public void subscribeOnReset(OnResetActivityListener listener) {
        this.onResetListeners.add(listener);
    }

    public void subscribeBeforeEvent(BeforeEventListener listener) {
        this.beforeEventListeners.add(listener);
    }

    public void subscribeOnEvent(OnEventListener listener) {
        this.onEventListeners.add(listener);
    }


    ///////////////////////////
    /* PUBLIC NOTIFY METHODS */
    ///////////////////////////

    public void beforeStartedActivity(ActivityRef activityRef) {
        for (BeforeStartedActivityListener listener : beforeStartedListeners) {
            listener.beforeStartedActivity(activityRef);
        }
    }

    public void onStartedActivity(ActivityRef activityRef) {
        for (OnStartedActivityListener listener : onStartedListeners) {
            listener.onStartedActivity(activityRef);
        }
    }

    public void beforeStoppedActivity(ActivityRef activityRef) {
        for (BeforeStoppedActivityListener listener : beforeStoppedListeners) {
            listener.beforeStoppedActivity(activityRef);
        }
    }

    public void onStoppedActivity(ActivityRef activityRef) {
        for (OnStoppedActivityListener listener : onStoppedListeners) {
            listener.onStoppedActivity(activityRef);
        }
    }

    public void onResetActivity(ActivityRef activityRef) {
        for (OnResetActivityListener listener : onResetListeners) {
            listener.onResetActivity(activityRef);
        }
    }

    public void beforeEventFired(EventRef eventRef) {
        for (BeforeEventListener listener : beforeEventListeners) {
            listener.beforeEvent(eventRef);
        }
    }

    public void onEventFired(EventRef eventRef) {
        for (OnEventListener listener : onEventListeners) {
            listener.onEvent(eventRef);
        }
    }

}
