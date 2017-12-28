package ru.citeck.ecos.flowable.listeners.global.impl.process.end;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableExecutionEntityNotificationSender;

/**
 * Cancel workflow listener
 */
public class CancelWorkflowListener implements GlobalEndExecutionListener {

    /**
     * Notification sender
     */
    private FlowableExecutionEntityNotificationSender sender;

    /**
     * Enabled
     */
    private boolean enabled;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        if(enabled) {
            ExecutionEntity entity = (ExecutionEntity) delegateExecution;
            Boolean value = (Boolean)entity.getVariable("cwf_sendNotification");
            if (Boolean.TRUE.equals(value)) {
                if(entity.isDeleted() && "cancelled".equals(entity.getDeleteReason())) {
                    sender.sendNotification((ExecutionEntity) delegateExecution);
                }
            }
        }
    }

    /**
     * Set notification sender
     * @param sender Sender
     */
    public void setSender(FlowableExecutionEntityNotificationSender sender) {
        this.sender = sender;
    }

    /**
     * Set enabled
     * @param enabled Enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
