package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.identitylink.service.IdentityLinkType;
import ru.citeck.ecos.deputy.TaskDeputyListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;
import ru.citeck.ecos.providers.ApplicationContextProvider;

import java.util.ArrayList;

/**
 * Task original owner listener
 */
public class TaskOriginalOwnerListener implements GlobalCreateTaskListener {

    /**
     * Set delegate listener name
     */
    private String delegateListenerName;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        Object originalOwner = delegateTask.getVariableLocal("taskOriginalOwner");
        String assignee = delegateTask.getAssignee();
        if (originalOwner == null) {
            delegateTask.setVariableLocal("taskOriginalOwner", assignee);
        }
        if (assignee != null) {
            TaskDeputyListener delegateListener = ApplicationContextProvider.getBean(delegateListenerName, TaskDeputyListener.class);
            ArrayList<String> actorsList = delegateListener.getActorsList(assignee);
            if (actorsList.size() > 1) {
                for (String actor : actorsList) {
                    delegateTask.addUserIdentityLink(actor, IdentityLinkType.CANDIDATE);
                }
                delegateTask.setAssignee(null);
            }
        }
    }

    /**
     * Set delegate listener name
     * @param delegateListenerName Delegate listener name
     */
    public void setDelegateListenerName(String delegateListenerName) {
        this.delegateListenerName = delegateListenerName;
    }
}
