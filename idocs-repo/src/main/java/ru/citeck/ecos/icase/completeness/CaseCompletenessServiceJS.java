package ru.citeck.ecos.icase.completeness;

import org.alfresco.repo.jscript.ScriptNode;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import static ru.citeck.ecos.utils.JavaScriptImplUtils.*;

public class CaseCompletenessServiceJS extends AlfrescoScopableProcessorExtension {
    
    private CaseCompletenessService impl;

    public ScriptNode[] getAllLevels(Object caseNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        return wrapNodes(impl.getAllLevels(caseRef), this);
    }
    
    public ScriptNode[] getCompletedLevels(Object caseNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        return wrapNodes(impl.getCompletedLevels(caseRef), this);
    }
    
    public ScriptNode[] getLevelRequirements(Object levelNode) {
        NodeRef levelRef = JavaScriptImplUtils.getNodeRef(levelNode);
        return wrapNodes(impl.getLevelRequirements(levelRef), this);
    }
    
    public ScriptNode[] getMatchedElements(Object caseNode, Object requirementNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        NodeRef reqRef = JavaScriptImplUtils.getNodeRef(requirementNode);
        return wrapNodes(impl.getRequirementMatchedElements(caseRef, reqRef), this);
    }
    
    public ScriptNode[] getCurrentLevels(Object caseNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        return wrapNodes(impl.getCurrentLevels(caseRef), this);
    }

    public boolean isLevelCompleted(Object caseNode, Object levelNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        NodeRef levelRef = JavaScriptImplUtils.getNodeRef(levelNode);
        return impl.isLevelCompleted(caseRef, levelRef);
    }

    public boolean isRequirementPassed(Object caseNode, Object reqNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        NodeRef reqRef = JavaScriptImplUtils.getNodeRef(reqNode);
        return impl.isRequirementPassed(caseRef, reqRef);
    }

    public ScriptNode[] getRequirementMatchedElements(Object caseNode, Object reqNode) {
        NodeRef caseRef = JavaScriptImplUtils.getNodeRef(caseNode);
        NodeRef reqRef = JavaScriptImplUtils.getNodeRef(reqNode);
        return wrapNodes(impl.getRequirementMatchedElements(caseRef, reqRef), this);
    }

    public void setImpl(CaseCompletenessService impl) {
        this.impl = impl;
    }
}
