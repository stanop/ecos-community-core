package ru.citeck.ecos.flowable.services.cmd;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.common.impl.interceptor.Command;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import ru.citeck.ecos.flowable.utils.FlowableUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetTaskListenersCmd implements Command<Map<String, List<FlowableListener>>>, Serializable {

    private static final long serialVersionUID = 1L;

    private String processDefinitionId;
    private List<String> tasks;

    public GetTaskListenersCmd() {
    }

    public GetTaskListenersCmd(String processDefinitionId, List<String> tasks) {
        this.processDefinitionId = processDefinitionId;
        this.tasks = tasks;
    }

    @Override
    public Map<String, List<FlowableListener>> execute(CommandContext commandContext) {

        if (processDefinitionId == null) {
            throw new IllegalStateException("processDefinitionId is null");
        }
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyMap();
        }

        Process process = FlowableUtils.getProcessByDefinitionId(processDefinitionId);

        Map<String, List<FlowableListener>> result = new HashMap<>();

        for (String task : tasks) {
            FlowElement flowTask = process.getFlowElement(task, true);
            List<FlowableListener> listeners = null;
            if (flowTask instanceof UserTask) {
                listeners = ((UserTask) flowTask).getTaskListeners();
            }
            result.put(task, listeners != null ? listeners : Collections.emptyList());
        }

        return result;
    }
}
