package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.events.EventConnection;
import ru.citeck.ecos.stream.event.EventFactory;
import ru.citeck.ecos.utils.TransactionUtils;

public class TaskEventListener extends AbstractTaskListener {

    private final EventConnection eventConnection;
    private final EventFactory eventFactory;

    @Autowired
    public TaskEventListener(EventConnection eventConnection, EventFactory eventFactory) {
        this.eventConnection = eventConnection;
        this.eventFactory = eventFactory;
    }

    @Override
    protected void notifyImpl(DelegateTask task) {
        eventFactory.fromActivitiTask(task)
                .ifPresent(eventDTO -> TransactionUtils.doAfterCommit(() -> eventConnection.emit(eventDTO)));
    }

}
