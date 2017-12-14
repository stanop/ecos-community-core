package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableExecutionEntityNotificationSender;

import java.util.Properties;

/**
 * Start workflow listener
 */
public class StartWorkflowListener implements GlobalStartExecutionListener {

    /**
     * Constants
     */
    private static final String NOTIFICATION_ENABLED_PROPERTY = "notification.start.workflow.enabled";

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    /**
     * Notification sender
     */
    private FlowableExecutionEntityNotificationSender sender;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        if(enabled()) {
            Boolean value = (Boolean) delegateExecution.getVariable("cwf_sendNotification");
            if (Boolean.TRUE.equals(value)) {
                sender.sendNotification((ExecutionEntity) delegateExecution);
            }
        }
    }

    /**
     * Is notifications enabled
     * @return Check result
     */
    private boolean enabled() {
        String enabledProperty = properties.getProperty(NOTIFICATION_ENABLED_PROPERTY);
        if (enabledProperty != null) {
            return Boolean.valueOf(enabledProperty);
        } else {
            return false;
        }
    }

    /**
     * Set notification sender
     * @param sender Notification sender
     */
    public void setSender(FlowableExecutionEntityNotificationSender sender) {
        this.sender = sender;
    }
}
