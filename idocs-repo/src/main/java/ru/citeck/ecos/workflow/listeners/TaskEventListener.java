package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.citeck.ecos.events.EventConnection;
import ru.citeck.ecos.stream.event.EventFactory;
import ru.citeck.ecos.utils.TransactionUtils;

/**
 * @author Roman Makarskiy
 */
public class TaskEventListener extends AbstractTaskListener {

    private final EventConnection eventConnection;
    private final EventFactory eventFactory;

    @Value("${event.task.create.emit.enabled}")
    private boolean eventTaskCreateEnabled;

    @Value("${event.task.assign.emit.enabled")
    private boolean eventTaskAssignEnabled;

    @Value("${event.task.complete.emit.enabled")
    private boolean eventTaskCompleteEnabled;

    @Value("${event.task.delete.emit.enabled")
    private boolean eventTaskDeleteEnabled;


    @Autowired
    public TaskEventListener(EventConnection eventConnection, EventFactory eventFactory) {
        this.eventConnection = eventConnection;
        this.eventFactory = eventFactory;
    }

    @Override
    protected void notifyImpl(DelegateTask task) {
        if (emitRequired(task)) {
            eventFactory.fromActivitiTask(task)
                    .ifPresent(eventDTO -> TransactionUtils.doAfterCommit(() -> eventConnection.emit(eventDTO)));
        }
    }

    private boolean emitRequired(DelegateTask task) {
        String eventName = task.getEventName();
        switch (eventName) {
            case TaskListener.EVENTNAME_CREATE:
                return eventTaskCreateEnabled;
            case TaskListener.EVENTNAME_ASSIGNMENT:
                return eventTaskAssignEnabled;
            case TaskListener.EVENTNAME_COMPLETE:
                return eventTaskCompleteEnabled;
            case TaskEventListener.EVENTNAME_DELETE:
                return eventTaskDeleteEnabled;
            default:
                return false;
        }
    }

}
