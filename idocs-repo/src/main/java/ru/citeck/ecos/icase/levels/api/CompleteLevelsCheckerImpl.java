package ru.citeck.ecos.icase.levels.api;

import org.alfresco.service.cmr.repository.NodeRef;

import ru.citeck.ecos.icase.completeness.CaseCompletenessService;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Nemerov
 * created on 17.03.2015.
 */
public class CompleteLevelsCheckerImpl implements CompleteLevelsChecker {

    private CaseCompletenessService completenessService;

    @Override
    public Set<NodeRef> checkCompletedLevels(NodeRef caseNode) {
        return completenessService.getCompletedLevels(caseNode);
    }

    @Override
    public Set<NodeRef> checkUncompletedLevels(NodeRef caseNode) {
        Set<NodeRef> completedLevels = completenessService.getCompletedLevels(caseNode);
        Set<NodeRef> allLevels = completenessService.getAllLevels(caseNode);
        Set<NodeRef> uncompletedLevels = new HashSet<>(allLevels.size());
        for (NodeRef level : allLevels) {
            if (!completedLevels.contains(level)) {
                uncompletedLevels.add(level);
            }
        }
        return uncompletedLevels;
    }

    @Override
    public boolean isCompleteLevel(NodeRef caseNode, NodeRef level) {
        return completenessService.isLevelCompleted(caseNode, level);
    }

    public void setCaseCompletenessService(CaseCompletenessService completenessService) {
        this.completenessService = completenessService;
    }
}
