package ru.citeck.ecos.flowable.action.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.TaskService;
import org.mozilla.javascript.JavaScriptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.action.group.impl.TxnGroupAction;
import ru.citeck.ecos.locks.LockUtils;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.AuthorityUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.citeck.ecos.flowable.constants.FlowableConstants.START_TASK_PREFIX;

/**
 * @author Pavel Simonov
 */
@Component
public class CompleteDocumentTaskAction implements GroupActionFactory<RecordRef> {

    public static final String ACTION_ID = "complete-document-task";
    public static final String TASKS = "tasks";

    private static final String MSG_TASKS_NOT_FOUND = "group-action.complete-doc-tasks.task-not-found";
    private static final String MSG_WITHOUT_WORKFLOW = "group-action.complete-doc-tasks.without-workflow";
    private static final String MSG_WITHOUT_USER_TASKS = "group-action.complete-doc-tasks.without-user-tasks";
    private static final String MSG_WITHOUT_ACTIVE_TASKS = "group-action.complete-doc-tasks.without-active-tasks";

    private static final String[] MANDATORY_PARAMS = { TASKS };

    private TaskService taskService;
    private AuthorityUtils authorityUtils;
    private WorkflowService workflowService;
    private TransactionService transactionService;
    private LockUtils lockUtils;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CompleteDocumentTaskAction(TransactionService transactionService,
                                      GroupActionService groupActionService,
                                      @Qualifier("WorkflowService")
                                              WorkflowService workflowService,
                                      AuthorityUtils authorityUtils,
                                      TaskService taskService,
                                      LockUtils lockUtils) {

        this.taskService = taskService;
        this.authorityUtils = authorityUtils;
        this.workflowService = workflowService;
        this.transactionService = transactionService;
        this.lockUtils = lockUtils;
        groupActionService.register(this);
    }

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String[] getMandatoryParams() {
        return MANDATORY_PARAMS;
    }

    @Override
    public GroupAction<RecordRef> createAction(GroupActionConfig config) {
        return new Action(config);
    }

    class Action extends TxnGroupAction<RecordRef> {

        private TasksParam tasksToComplete;
        private String defaultTransition;

        Action(GroupActionConfig config) {
            super(transactionService, config);

            try {
                tasksToComplete = objectMapper.readValue(config.getStrParam(TASKS), TasksParam.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < tasksToComplete.size(); i++) {
                TaskParam taskParam = tasksToComplete.get(i);
                if ("*".equals(taskParam.getTaskId())) {
                    tasksToComplete.remove(taskParam);
                    defaultTransition = taskParam.getTransition();
                    break;
                }
            }
        }

        @Override
        protected ActionStatus processImpl(RecordRef node) {

            NodeRef nodeRef = new NodeRef(node.getId());
            List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);
            if (workflows.isEmpty()) {
                return ActionStatus.skipped(MSG_WITHOUT_WORKFLOW);
            }
            WorkflowInstance workflow = workflows.get(0);

            WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
            taskQuery.setProcessId(workflow.getId());
            taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            taskQuery.setActive(true);

            List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery, true);

            if (tasks.isEmpty()) {
                return ActionStatus.skipped(MSG_WITHOUT_ACTIVE_TASKS);
            }

            Set<String> userAuthorities = authorityUtils.getUserAuthorities();

            tasks = tasks.stream().filter(task -> {
                String owner = (String) task.getProperties().get(ContentModel.PROP_OWNER);
                if (StringUtils.isNotBlank(owner)) {
                    return userAuthorities.contains(owner);
                }
                List<?> pooledActors = (List) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
                if (pooledActors == null) {
                    return false;
                }
                for (Object actor : pooledActors) {
                    if (actor instanceof NodeRef) {
                        String name = authorityUtils.getAuthorityName((NodeRef) actor);
                        if (name != null && userAuthorities.contains(name)) {
                            return true;
                        }
                    }
                }
                return false;
            }).collect(Collectors.toList());

            if (tasks.isEmpty()) {
                return ActionStatus.skipped(MSG_WITHOUT_USER_TASKS);
            }

            WorkflowTask taskToComplete = null;
            String transition = null;

            for (TaskParam param : tasksToComplete) {

                for (WorkflowTask task : tasks) {

                    String taskId = getTaskId(task);

                    if (param.getTaskId().equals(taskId)) {
                        taskToComplete = task;
                        transition = param.getTransition();
                        break;
                    }
                }
            }

            if (taskToComplete == null) {
                if (defaultTransition != null) {
                    taskToComplete = tasks.get(0);
                    transition = defaultTransition;
                } else {
                    return ActionStatus.skipped(MSG_TASKS_NOT_FOUND);
                }
            }

            try {

                if (taskToComplete.getId().startsWith("flowable$")) {

                    String outcomeField = "form_" + taskToComplete.getName() + "_outcome";

                    Map<String, Object> params = new HashMap<>();
                    params.put(outcomeField, transition);
                    params.put("outcome", transition);

                    String localId = taskToComplete.getId().replace("flowable$", "");

                    String lockId = String.format("%s-%s", "ECOSTask", localId);
                    lockUtils.doWithLock(lockId, () -> {
                        taskService.complete(localId, params);
                    });

                } else {

                    String lockId = String.format("%s-%s", "ECOSTask", taskToComplete.getId());
                    WorkflowTask finalTaskToComplete = taskToComplete;
                    String finalTransition = transition;
                    lockUtils.doWithLock(lockId, () -> {
                        workflowService.endTask(finalTaskToComplete.getId(), finalTransition);
                    });
                }
            } catch (RuntimeException e) {
                throw unwrapJavascriptException(e);
            }

            return ActionStatus.ok();
        }
    }

    private RuntimeException unwrapJavascriptException(RuntimeException exception) {

        Throwable ex = exception;

        while (ex.getCause() != null) {
            ex = ex.getCause();
        }

        if (ex instanceof JavaScriptException) {
            Object value = ((JavaScriptException) ex).getValue();
            return new RuntimeException(String.valueOf(value), exception);
        }

        return exception;
    }

    private String getTaskId(WorkflowTask task) {

        WorkflowTaskDefinition definition = task.getDefinition();
        if (definition == null) {
            return "";
        }

        WorkflowNode wfNode = definition.getNode();
        if (wfNode == null) {
            return "";
        }

        return wfNode.getName();
    }

    static class TasksParam extends ArrayList<TaskParam> {
    }

    static class TaskParam {

        @Getter
        @Setter
        private String transition;
        @Getter
        @Setter
        private String taskId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TaskParam taskParam = (TaskParam) o;
            return Objects.equals(transition, taskParam.transition) &&
                   Objects.equals(taskId, taskParam.taskId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transition, taskId);
        }
    }
}

