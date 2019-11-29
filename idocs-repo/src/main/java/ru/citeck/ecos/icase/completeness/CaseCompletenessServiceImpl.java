package ru.citeck.ecos.icase.completeness;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.icase.CaseUtils;
import ru.citeck.ecos.icase.completeness.current.CurrentLevelsResolver;
import ru.citeck.ecos.icase.completeness.dto.CaseDocumentDto;
import ru.citeck.ecos.icase.element.CaseElementDAO;
import ru.citeck.ecos.icase.element.CaseElementServiceImpl;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;
import ru.citeck.ecos.model.RequirementModel;
import ru.citeck.ecos.pred.PredicateService;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;
import java.util.stream.Collectors;

import static ru.citeck.ecos.constants.RequirementsConstants.*;

public class CaseCompletenessServiceImpl implements CaseCompletenessService {

    private static final String MODEL_CONTAINER = "space";
    private static final String MODEL_ELEMENT = "document";
    private static final String EXACTLY_ONE_QUANTIFIER_VALUE = "EXACTLY_ONE";
    private static final String EXISTS_QUANTIFIER_VALUE = "EXISTS";
    private static final String SINGLE_QUANTIFIER_VALUE = "SINGLE";

    private NodeService nodeService;
    private PredicateService predicateService;
    private DictionaryService dictionaryService;
    private CaseElementServiceImpl caseElementService;

    private final List<CurrentLevelsResolver> currentLevelsResolvers = new ArrayList<>();

    public void init() {
    }

    @Override
    public Set<NodeRef> getUncompletedLevels(NodeRef caseRef) {
        return getUncompletedLevels(caseRef, getCompletedLevels(caseRef));
    }

    @Override
    public Set<NodeRef> getUncompletedLevels(NodeRef caseRef, Collection<NodeRef> levelsToCheck) {

        Set<NodeRef> result = new HashSet<>();

        for (NodeRef level : levelsToCheck) {
            if (!isLevelCompleted(caseRef, level)) {
                result.add(level);
            }
        }

        return result;
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
        for (CurrentLevelsResolver resolver : currentLevelsResolvers) {
            Set<NodeRef> currentLevels = resolver.getCurrentLevels(caseNode);
            if (currentLevels != null) {
                return currentLevels;
            }
        }
        return Collections.emptySet();
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

    @Override
    public void register(CurrentLevelsResolver resolver) {
        this.currentLevelsResolvers.add(resolver);
    }

    @Override
    public Set<CaseDocumentDto> getCaseDocuments(NodeRef nodeRef) {

        Set<CaseDocumentDto> resultDtos = new HashSet<>();
        Set<NodeRef> currentLevels = this.getCurrentLevels(nodeRef);
        Set<NodeRef> allLevels = this.getAllLevels(nodeRef);

        for (NodeRef levelNodeRef : allLevels) {

            Set<NodeRef> levelRequirements = getLevelRequirements(levelNodeRef);

            for (NodeRef requirement : levelRequirements) {

                String quantifier = (String) nodeService.getProperty(requirement, QName.createQName(PRED_QUANTIFIER));

                List<ChildAssociationRef> childAssociations =
                        nodeService.getChildAssocs(requirement, Collections.singleton(QName.createQName(PRED_CONSEQUENT)));

                Set<CaseDocumentDto> extractedCaseDocumentDtos = childAssociations.stream()
                        .map(ChildAssociationRef::getChildRef)
                        .filter(this::isKindPredicate)
                        .map(childNodeRef -> childNodeRefToCaseDocumentDto(childNodeRef, quantifier,
                                currentLevels.contains(levelNodeRef)))
                        .collect(Collectors.toSet());

                resultDtos.addAll(extractedCaseDocumentDtos);
            }
        }

        return resultDtos;
    }

    private boolean isKindPredicate(NodeRef nodeRef) {
        return RepoUtils.isSubType(nodeRef, QName.createQName(PRED_KIND_PREDICATE), nodeService, dictionaryService);
    }

    private CaseDocumentDto childNodeRefToCaseDocumentDto(NodeRef nodeRef, String quantifier, boolean hasLevel) {
        boolean mandatory = false;
        if ((quantifier.equals(EXACTLY_ONE_QUANTIFIER_VALUE) || quantifier.equals(EXISTS_QUANTIFIER_VALUE)) && hasLevel) {
            mandatory = true;
        }

        boolean multiple = false;
        if (!quantifier.equals(EXACTLY_ONE_QUANTIFIER_VALUE) && !quantifier.equals(SINGLE_QUANTIFIER_VALUE)) {
            multiple = true;
        }

        String documentKind = removeNodeRefPrefix((String) nodeService.getProperty(nodeRef,
                QName.createQName(PRED_REQUIRED_KIND)));
        String documentType = removeNodeRefPrefix((String) nodeService.getProperty(nodeRef,
                QName.createQName(PRED_REQUIRED_TYPE)));

        String type = documentType;
        if (StringUtils.isNotBlank(documentKind)) {
            type += "/" + documentKind;
        }

        return new CaseDocumentDto(type, multiple, mandatory);
    }

    private String removeNodeRefPrefix(String nodeRef) {
        return nodeRef.replace("workspace://SpacesStore/", "");
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

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
