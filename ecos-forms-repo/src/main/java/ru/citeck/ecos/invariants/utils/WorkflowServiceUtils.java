package ru.citeck.ecos.invariants.utils;

import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * Workflow service utils
 */
public class WorkflowServiceUtils {

    /**
     * Constants
     */
    private static final String ID_SEPERATOR_REGEX = "\\$";

    /**
     * Workflow service
     */
    private WorkflowService workflowService;

    /**
     * Workflow admin service
     */
    private WorkflowAdminService workflowAdminService;

    /**
     * Get workflow definition by global name
     * @param workflowName Workflow name
     * @return Workflow definition
     */
    public WorkflowDefinition getWorkflowDefinition(String workflowName) {
        if (workflowName == null) {
            return null;
        }
        String parts[] = workflowName.split(ID_SEPERATOR_REGEX);
        if (parts.length != 2) {
            return null;
        }
        if (!workflowAdminService.isEngineEnabled(parts[0])) {
            return null;
        } else {
            return workflowService.getDefinitionByName(workflowName);
        }
    }

    /**
     * Set workflow service
     * @param workflowService Workflow service
     */
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Set workflow admin service
     * @param workflowAdminService Workflow admin service
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService) {
        this.workflowAdminService = workflowAdminService;
    }
}
