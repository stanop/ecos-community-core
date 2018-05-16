package ru.citeck.ecos.icase.completeness;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.element.CaseElementDAO;
import ru.citeck.ecos.icase.element.CaseElementServiceImpl;
import ru.citeck.ecos.icase.CaseUtils;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;
import ru.citeck.ecos.model.PredicateModel;
import ru.citeck.ecos.model.RequirementModel;
import ru.citeck.ecos.pred.PredicateService;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CaseCompletenessServiceImpl implements CaseCompletenessService {

    private static final Log LOGGER = LogFactory.getLog(CaseCompletenessService.class);

    private static final String MODEL_CONTAINER = "space";
    private static final String MODEL_ELEMENT = "document";

    private NodeService nodeService;
    private PredicateService predicateService;
    private CaseElementServiceImpl caseElementService;

    public void init() {
    }

    @Override
    public boolean isLevelsCompleted(NodeRef caseRef, Collection<NodeRef> levels) {
        return levels.stream().allMatch(levelRef -> isLevelCompleted(caseRef, levelRef));
    }

    @Override
    public boolean isLevelCompleted(NodeRef caseRef, NodeRef levelRef) {

        Set<NodeRef> levelRequirements = getLevelRequirements(levelRef);

        for (NodeRef req : levelRequirements) {
            if (!isRequirementPassed(caseRef, req)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<NodeRef> getAllLevels(NodeRef caseRef) {
        return toSet(RepoUtils.getTargetAssoc(caseRef, RequirementModel.ASSOC_COMPLETENESS_LEVELS, nodeService));
    }

    @Override
    public Set<NodeRef> getCompletedLevels(NodeRef caseRef) {
        return getAllLevels(caseRef).stream()
                                    .filter(levelRef -> isLevelCompleted(caseRef, levelRef))
                                    .collect(Collectors.toSet());
    }

    @Override
    public Set<NodeRef> getLevelRequirements(NodeRef levelRef) {
        List<NodeRef> requirements = RepoUtils.getChildrenByAssoc(levelRef,
                                                                  RequirementModel.ASSOC_LEVEL_REQUIREMENT,
                                                                  nodeService);
        return toSet(requirements);
    }

    @Override
    public Set<NodeRef> getCurrentLevels(NodeRef caseNode) {

        Set<NodeRef> completedLevels = getCompletedLevels(caseNode);
        Set<NodeRef> allLevels = getAllLevels(caseNode);
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

    @Override
    public boolean isRequirementPassed(NodeRef caseNode, NodeRef requirement) {
        Set<NodeRef> matches = getRequirementMatchedElements(caseNode, requirement);
        return predicateService.evaluateQuantifier(requirement, new ArrayList<>(matches));
    }

    @Override
    public Set<NodeRef> getRequirementMatchedElements(NodeRef caseRef, NodeRef requirement) {

        Map<ElementConfigDto, Collection<NodeRef>> elementsCache = new HashMap<>();
        Set<NodeRef> matchedElements = new HashSet<>();

        List<ElementConfigDto> elementConfigs = getRequirementScopes(requirement);

        for (ElementConfigDto elementConfig : elementConfigs) {
            Collection<NodeRef> elements = elementsCache.get(elementConfig);
            if (elements == null) {
                CaseElementDAO<ElementConfigDto> strategy = CaseUtils.getStrategy(elementConfig, caseElementService);
                elements = strategy.get(caseRef, elementConfig);
                elementsCache.put(elementConfig, elements);
            }
            for (NodeRef element : elements) {
                if (isElementMatched(caseRef, requirement, element)) {
                    matchedElements.add(element);
                }
            }
        }

        if (elementConfigs.isEmpty()) {
            if (isElementMatched(caseRef, requirement, caseRef)) {
                matchedElements.add(caseRef);
            }
        }

        return matchedElements;
    }

    private boolean isElementMatched(NodeRef caseRef, NodeRef requirement, NodeRef element) {
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_CONTAINER, caseRef);
        model.put(MODEL_ELEMENT, element);
        return predicateService.evaluatePredicate(requirement, model);
    }

    private List<ElementConfigDto> getRequirementScopes(NodeRef requirement) {
        List<NodeRef> configRefs = RepoUtils.getTargetAssoc(requirement, RequirementModel.ASSOC_REQUIREMENT_SCOPE, nodeService);
        List<ElementConfigDto> configs = new ArrayList<>();
        configRefs.forEach(ref -> caseElementService.getConfig(ref).ifPresent(configs::add));
        return configs;
    }

    private static <E> Set<E> toSet(List<E> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        if (list.size() == 1) {
            return Collections.singleton(list.get(0));
        }
        return new HashSet<>(list);
    }

    private boolean isCurrent(NodeRef level, Set<NodeRef> completedLevels) {

        Set<NodeRef> requirements = getLevelRequirements(level);

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
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Level " + level + " is locked by level " + requiredLevel +
                                         ", as it is " + (levelRequired ? "not completed" : "completed"));
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPredicateService(PredicateService predicateService) {
        this.predicateService = predicateService;
    }

    public void setCaseElementService(CaseElementServiceImpl caseElementService) {
        this.caseElementService = caseElementService;
    }
}