package ru.citeck.ecos.icase.levels.api;

import org.alfresco.repo.jscript.ScriptNode;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

/**
 * @author Alexander Nemerov
 * created on 17.03.2015.
 */
public class CompleteLevelsCheckerJSImpl extends AlfrescoScopableProcessorExtension
        implements CompleteLevelsCheckerJS{

    private CompleteLevelsChecker completeLevelsChecker;

    @Override
    public ScriptNode[] checkCompletedLevels(ScriptNode caseNode) {
        return JavaScriptImplUtils.wrapNodes(completeLevelsChecker
                .checkCompletedLevels(caseNode.getNodeRef()), this);
    }

    @Override
    public ScriptNode[] checkUncompletedLevels(ScriptNode caseNode) {
        return JavaScriptImplUtils.wrapNodes(completeLevelsChecker
                .checkUncompletedLevels(caseNode.getNodeRef()), this);
    }

    @Override
    public boolean isCompleteLevel(ScriptNode caseNode, ScriptNode level) {
        return completeLevelsChecker.isCompleteLevel(caseNode.getNodeRef(), level.getNodeRef());
    }

    public void setCompleteLevelsChecker(CompleteLevelsChecker completeLevelsChecker) {
        this.completeLevelsChecker = completeLevelsChecker;
    }
}
