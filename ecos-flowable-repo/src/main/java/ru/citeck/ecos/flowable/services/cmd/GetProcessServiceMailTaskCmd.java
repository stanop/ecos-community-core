package ru.citeck.ecos.flowable.services.cmd;

import lombok.NonNull;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.common.impl.interceptor.Command;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import ru.citeck.ecos.flowable.utils.FlowableUtils;

import java.io.Serializable;

/**
 * @author Roman Makarskiy
 */
public class GetProcessServiceMailTaskCmd implements Command<ServiceTask>, Serializable {

    private static final long serialVersionUID = -7076802077767588467L;

    private String processDefinitionId;
    private String serviceMailTaskId;

    public GetProcessServiceMailTaskCmd(@NonNull String processDefinitionId, @NonNull String serviceMailTaskId) {
        this.processDefinitionId = processDefinitionId;
        this.serviceMailTaskId = serviceMailTaskId;
    }

    @Override
    public ServiceTask execute(CommandContext commandContext) {
        Process process = FlowableUtils.getProcessByDefinitionId(processDefinitionId);

        FlowElement flowElement = process.getFlowElement(serviceMailTaskId);
        if (flowElement instanceof org.flowable.bpmn.model.ServiceTask) {
            return (ServiceTask) flowElement;
        }

        return null;
    }
}
