package ru.citeck.ecos.icase.activity;

import ru.citeck.ecos.behavior.activity.CaseTaskBehavior;

import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class CaseTaskAttributesMappingRegistrar {

    private CaseTaskBehavior caseTaskBehavior;

    private Map<String, Map<String, String>> attributesMappingByWorkflow;

    public void init() {
        caseTaskBehavior.registerAttributesMapping(attributesMappingByWorkflow);
    }

    public void setCaseTaskBehavior(CaseTaskBehavior caseTaskBehavior) {
        this.caseTaskBehavior = caseTaskBehavior;
    }

    public void setAttributesMappingByWorkflow(Map<String, Map<String, String>> attributesMappingByWorkflow) {
        this.attributesMappingByWorkflow = attributesMappingByWorkflow;
    }
}
