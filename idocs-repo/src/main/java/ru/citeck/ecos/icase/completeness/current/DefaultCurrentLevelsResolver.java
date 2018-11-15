package ru.citeck.ecos.icase.completeness.current;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.PredicateModel;
import ru.citeck.ecos.model.RequirementModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DefaultCurrentLevelsResolver extends AbstractCurrentLevelsResolver {

    private static final Log logger = LogFactory.getLog(DefaultCurrentLevelsResolver.class);

    private NodeService nodeService;

    @Override
    public Set<NodeRef> getCurrentLevels(NodeRef caseNode) {

        Set<NodeRef> completedLevels = caseCompletenessService.getCompletedLevels(caseNode);
        Set<NodeRef> allLevels = caseCompletenessService.getAllLevels(caseNode);
        Set<NodeRef> incompletedLevels = new HashSet<>(allLevels);
        incompletedLevels.removeAll(completedLevels);

        Set<NodeRef> currentLevels = new HashSet<>();
        for (NodeRef level : incompletedLevels) {
            if (isCurrent(level, completedLevels)) {
                currentLevels.add(level);
            }
        }

        return currentLevels;
    }

    private boolean isCurrent(NodeRef levelRef, Set<NodeRef> completedLevels) {

        Set<NodeRef> requirements = caseCompletenessService.getLevelRequirements(levelRef);

        for (NodeRef requirement : requirements) {

            List<?> requirementScope = nodeService.getTargetAssocs(requirement,
                                                                   RequirementModel.ASSOC_REQUIREMENT_SCOPE);

            // empty requirement scope means that it is requirement on case itself
            // only such requirements are relevant here
            if (!requirementScope.isEmpty()) {
                continue;
            }

            List<NodeRef> consequents = RepoUtils.getChildrenByAssoc(requirement,
                                                                     PredicateModel.ASSOC_CONSEQUENT,
                                                                     nodeService);
            for (NodeRef consequent : consequents) {
                Boolean levelRequired = RepoUtils.getProperty(consequent,
                                                              RequirementModel.PROP_LEVEL_REQUIRED,
                                                              nodeService);
                if (levelRequired == null) {
                    continue;
                }
                List<NodeRef> requiredLevels = RepoUtils.getTargetAssoc(consequent,
                                                                        RequirementModel.ASSOC_REQUIRED_LEVELS,
                                                                        nodeService);
                for (NodeRef requiredLevel : requiredLevels) {
                    if (completedLevels.contains(requiredLevel) != levelRequired) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Level " + levelRef + " is locked by level " + requiredLevel +
                                         ", as it is " + (levelRequired ? "not completed" : "completed"));
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
