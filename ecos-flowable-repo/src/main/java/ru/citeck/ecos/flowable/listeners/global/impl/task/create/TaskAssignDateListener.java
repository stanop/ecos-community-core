package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.flowable.engine.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;

import java.util.Date;

/**
 * Task assign date listener
 */
public class TaskAssignDateListener implements GlobalCreateTaskListener {

    /**
     * Is enabled
     */
    private boolean enabled;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        if(enabled) {
            Date start = new Date();
            delegateTask.setVariable("cwf:assignDate", start);
        }
    }

    /**
     * Set enabled
     * @param enabled Is listener enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled.booleanValue();
    }
}
