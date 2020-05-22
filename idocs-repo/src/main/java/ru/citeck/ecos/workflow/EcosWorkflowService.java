package ru.citeck.ecos.workflow;

import lombok.Getter;
import lombok.NonNull;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EcosWorkflowService {

    private Map<String, EngineWorkflowService> serviceByEngine = new ConcurrentHashMap<>();

    private WorkflowService workflowService;

    @Autowired
    public EcosWorkflowService(@Qualifier("WorkflowService") WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void sendSignal(NodeRef nodeRef, String signalName) {

        List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);

        if (workflows.isEmpty()) {
            throw new IllegalStateException("Active workflows is not found");
        }

        sendSignal(workflows.stream().map(WorkflowInstance::getId).collect(Collectors.toList()), signalName);
    }

    public void sendSignal(String processId, String signalName) {
        sendSignal(Collections.singletonList(processId), signalName);
    }

    public void sendSignal(Collection<String> processes, String signalName) {
        groupByEngineId(processes).forEach((engine, engineProcesses) -> {
            EngineWorkflowService engineService = needWorkflowService(engine);
            engineService.sendSignal(engineProcesses, signalName);
        });
    }

    private Map<String, List<String>> groupByEngineId(Collection<String> workflows) {

        Map<String, List<String>> result = new HashMap<>();

        for (String workflowId : workflows) {
            WorkflowId id = new WorkflowId(workflowId);
            result.computeIfAbsent(id.engineId, engineId -> new ArrayList<>()).add(id.localId);
        }

        return result;
    }

    private EngineWorkflowService needWorkflowService(String engineId) {
        EngineWorkflowService workflowService = serviceByEngine.get(engineId);
        if (workflowService == null) {
            throw new IllegalArgumentException("Workflow service for engine '" + engineId + "' is not registered");
        }
        return workflowService;
    }

    /**
     * Getting instance of workflow
     */
    public WorkflowInstance getInstanceById(@NonNull String workflowId) {
        return workflowService.getWorkflowById(workflowId);
    }

    public List<WorkflowInstance> getAllInstances(WorkflowInstanceQuery query, int max, int skipCount) {
        return workflowService.getWorkflows(query, max, skipCount);
    }

    public WorkflowInstance cancelWorkflowInstance(String workflowId) {
        return workflowService.cancelWorkflow(workflowId);
    }

    private static class WorkflowId {

        @Getter
        private final String engineId;
        @Getter
        private final String localId;

        WorkflowId(String workflowId) {
            int delimIdx = workflowId.indexOf('$');
            if (delimIdx == -1) {
                throw new IllegalArgumentException("Workflow id should has engine " +
                    "prefix. Workflow: '" + workflowId + "'");
            }
            this.engineId = workflowId.substring(0, delimIdx);
            this.localId = workflowId.substring(delimIdx + 1);
        }
    }

    public void register(String engine, EngineWorkflowService taskService) {
        serviceByEngine.put(engine, taskService);
    }
}
