package ru.citeck.ecos.workflow.tasks;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.locks.LockUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EcosTaskService {

    private static final String TASKS_PREFIX = "task-%s";

    public static final String FIELD_COMMENT = "comment";

    private static final String ASSIGNEE_NOT_MATCH_ERR_MSG_KEY = "ecos.task.complete.assignee.validation.error";

    private Map<String, EngineTaskService> taskServices = new ConcurrentHashMap<>();

    private LockUtils lockUtils;

    public void endTask(String taskId, Map<String, Object> variables) {
        endTask(taskId, null, variables, null);
    }

    public void endTask(String taskId, String transition) {
        endTask(taskId, transition, null, null);
    }

    public void endTask(String taskId, String transition, Map<String, Object> variables) {
        endTask(taskId, transition, variables, null);
    }

    public void endTask(String taskId,
                        String transition,
                        Map<String, Object> variables,
                        Map<String, Object> transientVariables) {

        ParameterCheck.mandatoryString("taskId", taskId);

        if (variables == null) {
            variables = Collections.emptyMap();
        }
        if (transientVariables == null) {
            transientVariables = Collections.emptyMap();
        }

        TaskId task = new TaskId(taskId);
        EngineTaskService taskService = needTaskService(task.getEngine());

        TaskInfo taskInfo = taskService.getTaskInfo(task.getLocalId());
        String assignee = taskInfo.getAssignee();

        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        if (assignee != null && user != null && !user.equals(AuthenticationUtil.getSystemUserName())) {
            if (!user.equals(assignee)) {
                throw new IllegalStateException(I18NUtil.getMessage(ASSIGNEE_NOT_MATCH_ERR_MSG_KEY));
            }
        }

        Map<String, Object> finalVariables = new HashMap<>(variables);
        Map<String, Object> finalTransientVariables = new HashMap<>(transientVariables);

        lockUtils.doWithLock(String.format(TASKS_PREFIX, taskId), () -> {
            taskService.endTask(task.getLocalId(), transition, finalVariables, finalTransientVariables);
        });
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
            int delimIdx = taskId.indexOf('$');
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

    @Autowired
    public void setLockUtils(LockUtils lockUtils) {
        this.lockUtils = lockUtils;
    }
}
