package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateTask;
import ru.citeck.ecos.events.EventConnection;
import ru.citeck.ecos.stream.event.EventFactory;
import ru.citeck.ecos.utils.TransactionUtils;

public class TaskEventListener extends AbstractTaskListener {

    private EventConnection eventConnection;
    private EventFactory eventFactory;

    @Override
    protected void notifyImpl(DelegateTask task) {
        eventFactory.fromActivitiTask(task)
                .ifPresent(eventDTO -> TransactionUtils.doAfterCommit(() -> eventConnection.emit(eventDTO)));
    }

    @Override
    protected void initImpl() {
        this.eventConnection = getBean("eventConnection", EventConnection.class);
        this.eventFactory = getBean(EventFactory.class);
    }
}
