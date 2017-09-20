package ru.citeck.ecos.icase;

import org.alfresco.repo.jscript.ScriptNode;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.*;

public class CaseCompletenessServiceJS extends AlfrescoScopableProcessorExtension {
    
    private CaseCompletenessService impl;

    public ScriptNode[] getAllLevels(ScriptNode caseNode) {
        return wrapNodes(impl.getAllLevels(caseNode.getNodeRef()), this);
    }
    
    public ScriptNode[] getCompletedLevels(ScriptNode caseNode) {
        return wrapNodes(impl.getCompletedLevels(caseNode.getNodeRef()), this);
    }
    
    public ScriptNode[] getLevelRequirements(ScriptNode levelNode) {
        return wrapNodes(impl.getLevelRequirements(levelNode.getNodeRef()), this);
    }
    
    public ScriptNode[] getPassedRequirements(ScriptNode caseNode) {
        return wrapNodes(impl.getPassedRequirements(caseNode.getNodeRef()), this);
    }
    
    public ScriptNode[] getMatchedElements(ScriptNode caseNode, ScriptNode requirement) {
        return wrapNodes(impl.getMatchedElements(caseNode.getNodeRef(), requirement.getNodeRef()), this);
    }
    
    public ScriptNode[] getCurrentLevels(ScriptNode caseNode) {
        return wrapNodes(impl.getCurrentLevels(caseNode.getNodeRef()), this);
    }
    
    public void recalculateLevels(ScriptNode caseNode) {
        impl.recalculateLevels(caseNode.getNodeRef());
    }

    public void recalculateLevel(ScriptNode caseNode, ScriptNode levelNode) {
        impl.recalculateLevel(caseNode.getNodeRef(), levelNode.getNodeRef());
    }

    public void recalculateRequirement(ScriptNode caseNode, ScriptNode requirement) {
        impl.recalculateRequirement(caseNode.getNodeRef(), requirement.getNodeRef());
    }

    public void setImpl(CaseCompletenessService impl) {
        this.impl = impl;
    }
    
}
