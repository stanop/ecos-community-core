package ru.citeck.ecos.icase;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.PredicateModel;
import ru.citeck.ecos.model.RequirementModel;
import ru.citeck.ecos.pred.PredicateService;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;

public class CaseCompletenessServiceImpl implements CaseCompletenessService, 
        CaseElementPolicies.OnCaseElementAddPolicy,
        CaseElementPolicies.OnCaseElementUpdatePolicy,
        CaseElementPolicies.OnCaseElementRemovePolicy
{

    private static final Log LOGGER = LogFactory.getLog(CaseCompletenessService.class);

    private static final String MODEL_CONTAINER = "space";
    private static final String MODEL_ELEMENT = "document";

    private NodeService nodeService;
    private PredicateService predicateService;
    private CaseElementServiceImpl caseElementService;
    private PolicyComponent policyComponent;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPredicateService(PredicateService predicateService) {
        this.predicateService = predicateService;
    }

    public void setCaseElementService(CaseElementServiceImpl caseElementService) {
        this.caseElementService = caseElementService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementAddPolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS,
                new ChainingJavaBehaviour(this, "onCaseElementAdd"));

        this.policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementUpdatePolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS,
                new ChainingJavaBehaviour(this, "onCaseElementUpdate"));

        this.policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementRemovePolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS,
                new ChainingJavaBehaviour(this, "onCaseElementRemove"));

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS, 
                new ChainingJavaBehaviour(this, "onCaseUpdate"));
        
        this.policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS, RequirementModel.ASSOC_COMPLETED_LEVELS,
                new ChainingJavaBehaviour(this, "onCaseUpdate"));
        
        this.policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS, RequirementModel.ASSOC_COMPLETED_LEVELS,
                new ChainingJavaBehaviour(this, "onCaseUpdate"));
        
        this.policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS, RequirementModel.ASSOC_COMPLETENESS_LEVELS, 
                new ChainingJavaBehaviour(this, "onLevelAdd"));
        
        this.policyComponent.bindAssociationBehaviour(NodeServicePolicies.BeforeDeleteAssociationPolicy.QNAME, 
                RequirementModel.ASPECT_HAS_COMPLETENESS_LEVELS, RequirementModel.ASSOC_COMPLETENESS_LEVELS, 
                new ChainingJavaBehaviour(this, "onLevelRemove"));
    }
    
    // shortcut getters
    private List<NodeRef> getElementMatches(NodeRef caseRef, NodeRef element) {
        return RepoUtils.getChildrenByProperty(caseRef, RequirementModel.PROP_MATCH_ELEMENT, element, nodeService);
    }
    
    private Collection<NodeRef> getLevelMatches(NodeRef caseRef, NodeRef level) {
        return RepoUtils.getChildrenByProperty(caseRef, RequirementModel.PROP_MATCH_LEVEL, level, nodeService);
    }
    
    private Map<NodeRef, NodeRef> makeMap(Collection<NodeRef> matches, QName property) {
        Map<NodeRef, NodeRef> map = new HashMap<>(matches.size());
        for(NodeRef match : matches) {
            NodeRef requirement = RepoUtils.getProperty(match, property, nodeService);
            map.put(requirement, match);
        }
        return map;
    }

    private List<NodeRef> getElementMatches(NodeRef caseRef, NodeRef element, NodeRef scope) {
        List<NodeRef> matches = getElementMatches(caseRef, element);
        List<NodeRef> result = new LinkedList<>();
        for(NodeRef match : matches) {
            NodeRef requirement = (NodeRef) nodeService.getProperty(match, RequirementModel.PROP_MATCH_REQUIREMENT);
            if(RepoUtils.isAssociated(requirement, scope, RequirementModel.ASSOC_REQUIREMENT_SCOPE, nodeService)) {
                result.add(match);
            }
        }
        return result;
    }

    private List<NodeRef> getRequirementMatches(NodeRef caseRef, NodeRef requirement) {
        return RepoUtils.getChildrenByProperty(caseRef, RequirementModel.PROP_MATCH_REQUIREMENT, requirement, nodeService);
    }
    
    private List<NodeRef> getRequirementScopes(NodeRef requirement) {
        return RepoUtils.getTargetAssoc(requirement, RequirementModel.ASSOC_REQUIREMENT_SCOPE, nodeService);
    }
    
    private String getMatchName(NodeRef element, NodeRef requirement) {
        return element.getId() + "_" + requirement.getId();
    }
    
    private List<NodeRef> getRequirementsForConfig(NodeRef caseRef, NodeRef config) {
        List<NodeRef> requirements = new LinkedList<>();
        for (NodeRef level : this.getAllLevels(caseRef)) {
            requirements.addAll(filterRequirementsByScope(getLevelRequirements(level), config));
        }
        return requirements;
    }

    private void createMatch(NodeRef caseRef, NodeRef requirement, NodeRef element) {
        String name = getMatchName(element, requirement);
        Map<QName, Serializable> matchProperties = new HashMap<>(4);
        matchProperties.put(ContentModel.PROP_NAME, name);
        matchProperties.put(RequirementModel.PROP_MATCH_ELEMENT, element);
        matchProperties.put(RequirementModel.PROP_MATCH_REQUIREMENT, requirement);
        NodeRef level = nodeService.getPrimaryParent(requirement).getParentRef();
        matchProperties.put(RequirementModel.PROP_MATCH_LEVEL, level);
        NodeRef match = nodeService.createNode(caseRef, RequirementModel.ASSOC_MATCHES, QName.createQName(ICaseModel.NAMESPACE, name),
                RequirementModel.TYPE_MATCH, matchProperties).getChildRef();
        LOGGER.debug("match created, case: " + caseRef + ", node: " + element + ", requirement: " + requirement + ", match: " + match);
    }

    private List<NodeRef> filterRequirementsByScope(Collection<NodeRef> requirements, NodeRef scope) {
        List<NodeRef> result = new LinkedList<>();
        if(scope != null) {
            for(NodeRef requirement : requirements) {
                if(RepoUtils.isAssociated(requirement, scope, RequirementModel.ASSOC_REQUIREMENT_SCOPE, nodeService)) {
                    result.add(requirement);
                }
            }
        } else {
            for(NodeRef requirement : requirements) {
                if(nodeService.getTargetAssocs(requirement, RequirementModel.ASSOC_REQUIREMENT_SCOPE).isEmpty()) {
                    result.add(requirement);
                }
            }
        }
        return result;
    }
    
    // checking matches and levels
    
    private Collection<NodeRef> checkLevelsForNode(NodeRef caseRef, NodeRef element, NodeRef config) {

        Set<NodeRef> levels = getAllLevels(caseRef);
        LOGGER.debug("found levels for case: " + caseRef + " levels: " + levels);
        
        Collection<NodeRef> requirementsForConfig = getRequirementsForConfig(caseRef, config);
        
        // process matches
        IncrementalNodeProcessor matchProcessor = new RequirementMatchProcessor(caseRef, element);
        Collection<NodeRef> affectedRequirements = process(matchProcessor, requirementsForConfig);
        
        return processRequirements(caseRef, affectedRequirements);
    }

    private Collection<NodeRef> processRequirements(NodeRef caseRef, Collection<NodeRef> requirements) {
        // process requirements
        RequirementProcessor requirementProcessor = new RequirementProcessor(caseRef);
        Collection<NodeRef> affectedLevels = process(requirementProcessor, requirements);
        
        // process levels
        LevelProcessor levelProcessor = new LevelProcessor(caseRef, requirementProcessor.getPassedRequirements());
        Collection<NodeRef> changedLevels = process(levelProcessor, affectedLevels);
        
        return changedLevels;
    }
    
    // abstract incremental processing
    
    private static Collection<NodeRef> process(IncrementalNodeProcessor processor, Collection<NodeRef> nodes) {
        Set<NodeRef> affectedNodes = new HashSet<NodeRef>(nodes.size() / 2);
        for(NodeRef node : nodes) {
            boolean isConnected = processor.isConnected(node);
            boolean shouldBeConnected = processor.shouldBeConnected(node);
            
            NodeRef affectedNode = null;
            if(!isConnected && shouldBeConnected) {
                affectedNode = processor.getAffectedNode(node);
                processor.connect(node);
            } else if(isConnected && !shouldBeConnected) {
                affectedNode = processor.getAffectedNode(node);
                processor.disconnect(node);
            }
            
            if(affectedNode != null)
                affectedNodes.add(affectedNode);
        }
        return affectedNodes;
    }
    
    private static Collection<NodeRef> disconnect(IncrementalNodeProcessor processor, Collection<NodeRef> nodes) {
        Set<NodeRef> affectedNodes = new HashSet<NodeRef>(nodes.size() / 2);
        for(NodeRef node : nodes) {
            if(!processor.isConnected(node)) continue;
            
            NodeRef affectedNode = processor.getAffectedNode(node);
            if(affectedNode != null)
                affectedNodes.add(affectedNode);
            processor.disconnect(node);
            
        }
        return affectedNodes;
    }
    
    private interface IncrementalNodeProcessor {
        
        public boolean isConnected(NodeRef nodeRef);
        
        public boolean shouldBeConnected(NodeRef nodeRef);
        
        public void connect(NodeRef nodeRef);
        
        public void disconnect(NodeRef nodeRef);
        
        public NodeRef getAffectedNode(NodeRef nodeRef);
        
    }
    
    // concrete processors: match, requirement and level processing
    
    private class RequirementMatchProcessor implements IncrementalNodeProcessor {
        
        private final NodeRef caseRef, element;
        private final Map<NodeRef, NodeRef> persistedMatches;
        
        public RequirementMatchProcessor(NodeRef caseRef, NodeRef element) {
            this.caseRef = caseRef;
            this.element = element;
            this.persistedMatches = makeMap(getElementMatches(caseRef, element), RequirementModel.PROP_MATCH_REQUIREMENT);
        }

        public RequirementMatchProcessor(NodeRef caseRef, Collection<NodeRef> matches) {
            this.caseRef = caseRef;
            this.element = null;
            this.persistedMatches = makeMap(matches, RequirementModel.PROP_MATCH_REQUIREMENT);
        }
        
        @Override
        public boolean isConnected(NodeRef requirement) {
            return persistedMatches.containsKey(requirement);
        }

        @Override
        public boolean shouldBeConnected(NodeRef requirement) {
            Set<NodeRef> pendingDelete = TransactionalResourceHelper.getSet(DbNodeServiceImpl.KEY_PENDING_DELETE_NODES);
            if (!pendingDelete.contains(element)) {
            Map<String, Object> model = new HashMap<>();
            model.put(MODEL_CONTAINER, caseRef);
            model.put(MODEL_ELEMENT, element);
            return predicateService.evaluatePredicate(requirement, model);
        }
            return false;
        }

        @Override
        public void connect(NodeRef requirement) {
            createMatch(caseRef, requirement, element);
        }

        @Override
        public void disconnect(NodeRef requirement) {
            NodeRef match = persistedMatches.get(requirement);
            if(match == null) return;
            RepoUtils.deleteNode(match, nodeService);
            LOGGER.debug("match deleted, case: " + caseRef + ", element: " + element + ", match: " + match);
        }

        @Override
        public NodeRef getAffectedNode(NodeRef requirement) {
            return requirement;
        }
        
    }
    
    private class ElementMatchProcessor implements IncrementalNodeProcessor {
        
        private final NodeRef caseRef, requirement;
        private final Map<NodeRef, NodeRef> persistedMatches;
        
        public ElementMatchProcessor(NodeRef caseRef, NodeRef requirement) {
            this.caseRef = caseRef;
            this.requirement = requirement;
            this.persistedMatches = makeMap(getRequirementMatches(caseRef, requirement), RequirementModel.PROP_MATCH_ELEMENT);
        }

        @Override
        public boolean isConnected(NodeRef element) {
            return persistedMatches.containsKey(element);
        }

        @Override
        public boolean shouldBeConnected(NodeRef element) {
            Map<String, Object> model = new HashMap<>();
            model.put(MODEL_CONTAINER, caseRef);
            model.put(MODEL_ELEMENT, element);
            return predicateService.evaluatePredicate(requirement, model);
        }

        @Override
        public void connect(NodeRef element) {
            createMatch(caseRef, requirement, element);
        }

        @Override
        public void disconnect(NodeRef element) {
            NodeRef match = persistedMatches.get(element);
            if(match == null) return;
            RepoUtils.deleteNode(match, nodeService);
            LOGGER.debug("match deleted, case: " + caseRef + ", requirement: " + requirement + ", match: " + match);
        }

        @Override
        public NodeRef getAffectedNode(NodeRef element) {
            return requirement;
        }
        
    }
    
    private class RequirementProcessor implements IncrementalNodeProcessor {
        
        private final NodeRef caseRef;
        private final Set<NodeRef> passedRequirements;
        
        public RequirementProcessor(NodeRef caseRef) {
            this.caseRef = caseRef;
            this.passedRequirements = new HashSet<>(RepoUtils.getTargetAssoc(caseRef, RequirementModel.ASSOC_PASSED_REQUIREMENTS, nodeService));
        }
        
        private Set<NodeRef> getPassedRequirements() {
            return passedRequirements;
        }

        @Override
        public boolean isConnected(NodeRef requirement) {
            return passedRequirements.contains(requirement);
        }

        @Override
        public boolean shouldBeConnected(NodeRef requirement) {
            List<NodeRef> matches = getRequirementMatches(caseRef, requirement);
            return predicateService.evaluateQuantifier(requirement, matches);
        }

        @Override
        public void connect(NodeRef requirement) {
            nodeService.createAssociation(caseRef, requirement, RequirementModel.ASSOC_PASSED_REQUIREMENTS);
            passedRequirements.add(requirement);
        }

        @Override
        public void disconnect(NodeRef requirement) {
            nodeService.removeAssociation(caseRef, requirement, RequirementModel.ASSOC_PASSED_REQUIREMENTS);
            passedRequirements.remove(requirement);
        }

        @Override
        public NodeRef getAffectedNode(NodeRef requirement) {
            return RepoUtils.getPrimaryParentRef(requirement, nodeService);
        }
        
    }
    
    private class LevelProcessor implements IncrementalNodeProcessor {
        
        private final NodeRef caseRef;
        private final Set<NodeRef> passedRequirements;
        private final Set<NodeRef> completedLevels;
        
        public LevelProcessor(NodeRef caseRef, Set<NodeRef> passedRequirements) {
            this.caseRef = caseRef;
            this.passedRequirements = passedRequirements;
            this.completedLevels = getCompletedLevels(caseRef);
        }

        @Override
        public boolean isConnected(NodeRef level) {
            return completedLevels.contains(level);
        }

        @Override
        public boolean shouldBeConnected(NodeRef level) {
            LOGGER.debug("checking level: " + level);
            Set<NodeRef> levelRequirements = getLevelRequirements(level);
            
            boolean levelComplete = passedRequirements.containsAll(levelRequirements);
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("level: " + level + " is " + (levelComplete ? "complete" : "not complete"));
            }
            
            return levelComplete;
        }

        @Override
        public void connect(NodeRef level) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("level " + level + " is added to completed");
            }
            nodeService.createAssociation(caseRef, level, RequirementModel.ASSOC_COMPLETED_LEVELS);
        }
        
        @Override
        public void disconnect(NodeRef level) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("level " + level + " is removed from completed");
            }
            nodeService.removeAssociation(caseRef, level, RequirementModel.ASSOC_COMPLETED_LEVELS);
        }
        
        @Override
        public NodeRef getAffectedNode(NodeRef level) {
            return level;
        }
        
    }

    // event handlers
    
    @Override
    public void onCaseElementAdd(NodeRef caseRef, NodeRef element, NodeRef config) {
        if (!nodeService.exists(caseRef) || !nodeService.exists(element) || !nodeService.exists(config)) {
            return;
        }
        
        LOGGER.debug("--START checking node: " + element);
        checkLevelsForNode(caseRef, element, config);
        LOGGER.debug("--END checking node: " + element);
    }

    @Override
    public void onCaseElementUpdate(NodeRef caseRef, NodeRef element, NodeRef config) {
        if (!nodeService.exists(caseRef) || !nodeService.exists(element) || !nodeService.exists(config)) {
            return;
        }
        
        LOGGER.debug("--START checking node: " + element);
        checkLevelsForNode(caseRef, element, config);
        LOGGER.debug("--END checking node: " + element);
    }

    @Override
    public void onCaseElementRemove(NodeRef caseRef, NodeRef element, NodeRef config) {
        if (!nodeService.exists(caseRef) || !nodeService.exists(element) || !nodeService.exists(config)) {
            return;
        }
        
        List<NodeRef> elementMatches = getElementMatches(caseRef, element, config);
        
        // process matches
        RequirementMatchProcessor matchProcessor = new RequirementMatchProcessor(caseRef, elementMatches);
        Collection<NodeRef> affectedRequirements = disconnect(matchProcessor, matchProcessor.persistedMatches.keySet());
        
        processRequirements(caseRef, affectedRequirements);
        
    }

    public void onCaseUpdate(NodeRef caseRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(caseRef)) {
            return;
        }
        
        LOGGER.debug("### START checking case: " + caseRef);
        checkLevelsForNode(caseRef, caseRef, null);
        LOGGER.debug("### END checking case: " + caseRef);
    }
    
    public void onCaseUpdate(AssociationRef assocRef) {
        NodeRef caseRef = assocRef.getSourceRef();
        if (!nodeService.exists(caseRef)) {
            return;
        }
        
        LOGGER.debug("### START checking case: " + caseRef);
        checkLevelsForNode(caseRef, caseRef, null);
        LOGGER.debug("### END checking case: " + caseRef);
    }
    
    public void onLevelAdd(AssociationRef assocRef) {
        NodeRef caseNode = assocRef.getSourceRef();
        NodeRef level = assocRef.getTargetRef();
        recalculateLevel(caseNode, level);
    }
    
    public void onLevelRemove(AssociationRef assocRef) {
        NodeRef caseNode = assocRef.getSourceRef();
        NodeRef level = assocRef.getTargetRef();
        
        Collection<NodeRef> levelMatches = getLevelMatches(caseNode, level);
        
        // process matches
        RequirementMatchProcessor matchProcessor = new RequirementMatchProcessor(caseNode, levelMatches);
        Collection<NodeRef> affectedRequirements = disconnect(matchProcessor, levelMatches);
        
        // process requirements
        RequirementProcessor requirementProcessor = new RequirementProcessor(caseNode);
        Collection<NodeRef> affectedLevels = disconnect(requirementProcessor, affectedRequirements);
        
        // process levels
        LevelProcessor levelProcessor = new LevelProcessor(caseNode, requirementProcessor.getPassedRequirements());
        Collection<NodeRef> changedLevels = disconnect(levelProcessor, affectedLevels);
    }
    
    // service interface

    @Override
    public Set<NodeRef> getAllLevels(NodeRef caseRef) {
        return toSet(RepoUtils.getTargetAssoc(caseRef, RequirementModel.ASSOC_COMPLETENESS_LEVELS, nodeService));
    }
    
    @Override
    public Set<NodeRef> getCompletedLevels(NodeRef caseRef) {
        return toSet(RepoUtils.getTargetAssoc(caseRef, RequirementModel.ASSOC_COMPLETED_LEVELS, nodeService));
    }

    @Override
    public Set<NodeRef> getLevelRequirements(NodeRef levelRef) {
        return toSet(RepoUtils.getChildrenByAssoc(levelRef, RequirementModel.ASSOC_LEVEL_REQUIREMENT, nodeService));
    }

    @Override
    public Set<NodeRef> getPassedRequirements(NodeRef caseRef) {
        return toSet(RepoUtils.getTargetAssoc(caseRef, RequirementModel.ASSOC_PASSED_REQUIREMENTS, nodeService));
    }

    @Override
    public Set<NodeRef> getMatchedElements(NodeRef caseRef, NodeRef requirement) {
        List<NodeRef> matches = getRequirementMatches(caseRef, requirement);
        Set<NodeRef> elements = new HashSet<>(matches.size());
        for(NodeRef match : matches) {
            elements.add((NodeRef) nodeService.getProperty(match, RequirementModel.PROP_MATCH_ELEMENT));
        }
        return elements;
    }
    
    private static <E> Set<E> toSet(List<E> list) {
        if(list == null || list.isEmpty()) return Collections.emptySet();
        if(list.size() == 1) return Collections.singleton(list.get(0));
        return new HashSet<>(list);
    }

    private boolean isCurrent(NodeRef level, Set<NodeRef> completedLevels) {
        Set<NodeRef> requirements = getLevelRequirements(level);
        for (NodeRef requirement : requirements) {
            List<?> requirementScope = nodeService.getTargetAssocs(requirement, RequirementModel.ASSOC_REQUIREMENT_SCOPE);
            
            // empty requirement scope means that it is requirement on case itself
            // only such requirements are relevant here
            if(!requirementScope.isEmpty()) continue;
            
            List<NodeRef> consequents = RepoUtils.getChildrenByAssoc(requirement, PredicateModel.ASSOC_CONSEQUENT, nodeService);
            for (NodeRef consequent : consequents) {
                Boolean levelRequired = RepoUtils.getProperty(consequent, RequirementModel.PROP_LEVEL_REQUIRED, nodeService);
                if(levelRequired == null) continue;
                List<NodeRef> requiredLevels = RepoUtils.getTargetAssoc(consequent, RequirementModel.ASSOC_REQUIRED_LEVELS, nodeService);
                for(NodeRef requiredLevel : requiredLevels) {
                    if(completedLevels.contains(requiredLevel) != levelRequired) {
                        if(LOGGER.isDebugEnabled())
                            LOGGER.debug("Level " + level + " is locked by level " + requiredLevel + ", as it is " + (levelRequired ? "not completed" : "completed"));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Set<NodeRef> getCurrentLevels(NodeRef caseNode) {
        Set<NodeRef> completedLevels = getCompletedLevels(caseNode);
        Set<NodeRef> allLevels = getAllLevels(caseNode);
        Set<NodeRef> incompletedLevels = new HashSet<>(allLevels);
        incompletedLevels.removeAll(completedLevels);

        Set<NodeRef> currentLevels = new HashSet<NodeRef>();
        for (NodeRef level : incompletedLevels) {
            if (isCurrent(level, completedLevels)) {
                currentLevels.add(level);
            }
        }
        return currentLevels;
    }

    @Override
    public void recalculateLevels(NodeRef caseNode) {
        new ForcedCalculator(caseNode).recalculateLevels();
    }

    @Override
    public void recalculateLevel(NodeRef caseNode, NodeRef levelRef) {
        new ForcedCalculator(caseNode).recalculateLevel(levelRef);
    }

    @Override
    public void recalculateRequirement(NodeRef caseNode, NodeRef requirement) {
        new ForcedCalculator(caseNode).recalculateRequirement(requirement);
    }
    
    // class that supports forced recalculation of requirements and levels
    private class ForcedCalculator {
        
        private NodeRef caseNode;
        private Map<NodeRef, Collection<NodeRef>> elementsCache;
        private RequirementProcessor requirementProcessor;
        private LevelProcessor levelProcessor;
        

        public ForcedCalculator(NodeRef caseNode) {
            this.caseNode = caseNode;
            this.elementsCache = new HashMap<>();
            this.requirementProcessor = new RequirementProcessor(caseNode);
            this.levelProcessor = new LevelProcessor(caseNode, requirementProcessor.getPassedRequirements());
        }
        
        public void recalculateRequirement(NodeRef requirement) {
            NodeRef level = nodeService.getPrimaryParent(requirement).getParentRef();
            recalculateRequirementMatches(requirement);
            process(requirementProcessor, Collections.singleton(requirement));
            process(levelProcessor, Collections.singleton(level));
        }
        
        public void recalculateLevel(NodeRef levelRef) {
            Set<NodeRef> requirements = getLevelRequirements(levelRef);
            for(NodeRef requirement : requirements) {
                recalculateRequirementMatches(requirement);
            }
            process(requirementProcessor, requirements);
            process(levelProcessor, Collections.singleton(levelRef));
        }
        
        public void recalculateLevels() {
            Set<NodeRef> levels = getAllLevels(caseNode);
            for(NodeRef levelRef : levels) {
                recalculateLevel(levelRef);
            }
        }

        private void recalculateRequirementMatches(NodeRef requirement) {
            ElementMatchProcessor processor = new ElementMatchProcessor(caseNode, requirement);
            
            List<NodeRef> elementConfigs = getRequirementScopes(requirement);
            // process elements:
            for(NodeRef elementConfig : elementConfigs) {
                Collection<NodeRef> elements = elementsCache.get(elementConfig);
                if(elements == null) {
                    CaseElementDAO strategy = CaseUtils.getStrategy(elementConfig, caseElementService);
                    elements = strategy.get(caseNode, elementConfig);
                    elementsCache.put(elementConfig, elements);
                }
                process(processor, elements);
            }
            
            if(elementConfigs.isEmpty()) {
                process(processor, Collections.singleton(caseNode));
            }
        }
        
    }

}