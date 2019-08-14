package ru.citeck.ecos.workflow.tasks;

import lombok.Getter;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.utils.WorkflowUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EcosTaskService {

    public static final String FIELD_COMMENT = "comment";

    private Map<String, EngineTaskService> taskServices = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;
    @Autowired
    private WorkflowUtils workflowUtils;

    public void endTask(String taskId, String transition, Map<String, Object> variables) {

        TaskId task = new TaskId(taskId);
        EngineTaskService taskService = needTaskService(task.getEngine());

        if (variables == null) {
            variables = new HashMap<>();
        } else {
            variables = new HashMap<>(variables);
        }

        taskService.endTask(task.getLocalId(), transition, variables);
    }

    public Optional<TaskInfo> getTaskInfo(String taskId) {
        TaskId task = new TaskId(taskId);
        EngineTaskService taskService = needTaskService(task.getEngine());
        TaskInfo taskInfo = taskService.getTaskInfo(task.getLocalId());
        if (taskInfo != null) {
            taskInfo = new EngineTaskInfo(task.getEngine(), taskInfo);
        }
        return Optional.ofNullable(taskInfo);
    }

    private EngineTaskService needTaskService(String engineId) {
        EngineTaskService taskService = taskServices.get(engineId);
        if (taskService == null) {
            throw new IllegalArgumentException("Task service for engine '" + engineId + "' is not registered");
        }
        return taskService;
    }

    private static class TaskId {

        @Getter private final String engine;
        @Getter private final String localId;

        TaskId(String taskId) {
            int delimIdx = taskId.indexOf("$");
            if (delimIdx == -1) {
                throw new IllegalArgumentException("Task id should has engine prefix. Task: '" + taskId + "'");
            }
            this.engine = taskId.substring(0, delimIdx);
            this.localId = taskId.substring(delimIdx + 1);
        }
    }

    public void register(String engine, EngineTaskService taskService) {
        taskServices.put(engine, taskService);
    }
}
