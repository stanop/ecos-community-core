package ru.citeck.ecos.flowable.services.completeness;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.FlowableWorkflowComponent;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.listeners.CheckListsTaskListener;
import ru.citeck.ecos.flowable.services.FlowableHistoryService;
import ru.citeck.ecos.flowable.services.cmd.GetTaskListenersCmd;
import ru.citeck.ecos.icase.completeness.current.AbstractCurrentLevelsResolver;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FlowableCurrentLevelsResolver extends AbstractCurrentLevelsResolver {

    private static final String EVENT_COMPLETE = "complete";
    private static final String PARAM_LISTS = "lists";

    private FlowableWorkflowComponent workflowComponent;
    private WorkflowPackageComponent workflowPackageComponent;
    private FlowableHistoryService flowableHistoryService;

    private ProcessEngineConfiguration engineConfiguration;

    @Override
    public Set<NodeRef> getCurrentLevels(NodeRef caseNode) {
        return AuthenticationUtil.runAsSystem(() -> getCurrentLevelsImpl(caseNode));
    }

    private Set<NodeRef> getCurrentLevelsImpl(NodeRef caseNode) {

        List<WorkflowInstance> workflows = workflowPackageComponent.getWorkflowIdsForContent(caseNode)
                                                                   .stream()
                                                                   .filter(this::isFlowableId)
                                                                   .map(this::getWorkflow)
                                                                   .filter(Optional::isPresent)
                                                                   .map(Optional::get)
                                                                   .filter(WorkflowInstance::isActive)
                                                                   .collect(Collectors.toList());
        if (workflows.isEmpty()) {
            return null;
        }

        Set<NodeRef> levels = new HashSet<>();

        for (WorkflowInstance workflow : workflows) {

            WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
            taskQuery.setActive(true);
            taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            taskQuery.setProcessId(workflow.getId().replace(FlowableConstants.ENGINE_PREFIX, ""));

            List<HistoricTaskInstance> tasks = flowableHistoryService.getTasksByQuery(taskQuery);

            List<String> taskIds = tasks.stream()
                                        .map(TaskInfo::getTaskDefinitionKey)
                                        .collect(Collectors.toList());

            String workflowDefId = workflow.getDefinition()
                                           .getId()
                                           .replace(FlowableConstants.ENGINE_PREFIX, "");

            GetTaskListenersCmd cmd = new GetTaskListenersCmd(workflowDefId, taskIds);
            Map<String, List<FlowableListener>> listeners = engineConfiguration.getCommandExecutor().execute(cmd);

            listeners.forEach((taskDefId, taskListeners) -> {

                for (FlowableListener listener : taskListeners) {

                    if (EVENT_COMPLETE.equals(listener.getEvent())) {

                        String implType = listener.getImplementation();
                        if (CheckListsTaskListener.class.getName().equals(implType)) {

                            List<FieldExtension> fields = listener.getFieldExtensions();

                            if (fields != null) {

                                for (FieldExtension field : fields) {

                                    if (PARAM_LISTS.equals(field.getFieldName())) {
                                        String lists = field.getStringValue();
                                        CheckListsTaskListener.fillNodeRefs(levels, lists);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        return levels;
    }

    private Optional<WorkflowInstance> getWorkflow(String id) {
        return Optional.ofNullable(workflowComponent.getWorkflowById(id));
    }

    private boolean isFlowableId(String id) {
        return id.startsWith(FlowableConstants.ENGINE_PREFIX);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Autowired
    public void setWorkflowComponent(FlowableWorkflowComponent workflowComponent) {
        this.workflowComponent = workflowComponent;
    }

    @Autowired
    @Qualifier("workflowPackageImpl")
    public void setWorkflowPackageComponent(WorkflowPackageComponent workflowPackageComponent) {
        this.workflowPackageComponent = workflowPackageComponent;
    }

    @Autowired
    public void setFlowableHistoryService(FlowableHistoryService flowableHistoryService) {
        this.flowableHistoryService = flowableHistoryService;
    }

    @Autowired
    @Qualifier("flowableEngineConfiguration")
    public void setEngineConfiguration(ProcessEngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }
}
