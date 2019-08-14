package ru.citeck.ecos.flowable.cmd;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class SendWorkflowSignalCmd implements Command<Void> {

    private static final Logger logger = LoggerFactory.getLogger(SendWorkflowSignalCmd.class);

    private final String signalName;
    private final List<String> workflows;
    private final boolean async;

    public SendWorkflowSignalCmd(List<String> workflows, String signalName, boolean async) {
        this.signalName = signalName;
        this.workflows = workflows;
        this.async = async;
    }

    @Override
    public Void execute(CommandContext context) {

        if (workflows == null || workflows.isEmpty()) {
            return null;
        }

        ExecutionEntityManager entityMngr = CommandContextUtil.getExecutionEntityManager(context);
        EventSubscriptionEntityManager eventsMngr = CommandContextUtil.getEventSubscriptionEntityManager(context);

        for (String workflow : workflows) {
            sendWorkflowSignal(eventsMngr, entityMngr, workflow);
        }

        return null;
    }

    private void sendWorkflowSignal(EventSubscriptionEntityManager eventsMngr,
                                    ExecutionEntityManager entityMngr,
                                    String workflowId) {

        ExecutionEntity execution = entityMngr.findByRootProcessInstanceId(workflowId);

        if (execution == null) {
            String msg = "Cannot find execution with id '" + workflowId + "'";
            throw new FlowableObjectNotFoundException(msg, Execution.class);
        }

        if (execution.isSuspended()) {

            String msg = "Cannot throw signal event '" + signalName +
                         "' because execution '" + workflowId + "' is suspended";

            logger.error(msg);
            return;
        }

        List<SignalEventSubscriptionEntity> signals =
                eventsMngr.findSignalEventSubscriptionsByProcessInstanceAndEventName(workflowId, signalName);

        for (SignalEventSubscriptionEntity signal : signals) {
            eventsMngr.eventReceived(signal, null, async);
        }
    }
}