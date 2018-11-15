package ru.citeck.ecos.flowable.services.cmd;

import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.common.api.FlowableObjectNotFoundException;
import org.flowable.engine.common.impl.interceptor.Command;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;

import java.io.Serializable;
import java.util.*;

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

        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);

        if (bpmnModel == null) {
            throw new FlowableObjectNotFoundException("Cannot find bpmn model for process definition id: " +
                                                      processDefinitionId, BpmnModel.class);
        }

        Process process = bpmnModel.getMainProcess();

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
