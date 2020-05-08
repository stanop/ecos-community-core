package ru.citeck.ecos.icase.activity;

import ru.citeck.ecos.behavior.activity.CaseTaskBehavior;
import ru.citeck.ecos.icase.activity.service.eproc.listeners.activity.CaseTaskListener;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class CaseTaskAttributesMappingRegistrar {

    private CaseTaskBehavior caseTaskBehavior;
    private CaseTaskListener caseTaskListener;

    private Map<String, Map<String, String>> attributesMappingByWorkflow;
    private Map<String, List<String>> workflowTransmittedVariables;

    public void init() {
        if(attributesMappingByWorkflow != null) {
            if (caseTaskBehavior != null) {
                caseTaskBehavior.registerAttributesMapping(attributesMappingByWorkflow);
            }
            if (caseTaskListener != null) {
                caseTaskListener.registerAttributesMapping(attributesMappingByWorkflow);
            }
        }
        if(workflowTransmittedVariables != null) {
            if (caseTaskBehavior != null) {
                caseTaskBehavior.registerWorkflowTransmittedVariables(workflowTransmittedVariables);
            }
            if (caseTaskListener != null) {
                caseTaskListener.registerWorkflowTransmittedVariables(workflowTransmittedVariables);
            }
        }
    }

    public void setCaseTaskBehavior(CaseTaskBehavior caseTaskBehavior) {
        this.caseTaskBehavior = caseTaskBehavior;
    }

    public void setCaseTaskListener(CaseTaskListener caseTaskListener) {
        this.caseTaskListener = caseTaskListener;
    }

    public void setAttributesMappingByWorkflow(Map<String, Map<String, String>> attributesMappingByWorkflow) {
        this.attributesMappingByWorkflow = attributesMappingByWorkflow;
    }

    /**
     * @param workflowTransmittedVariables variables which would be transmitted from completed task to next
     */
    public void setWorkflowTransmittedVariables(Map<String, List<String>> workflowTransmittedVariables) {
        this.workflowTransmittedVariables = workflowTransmittedVariables;
    }

}
