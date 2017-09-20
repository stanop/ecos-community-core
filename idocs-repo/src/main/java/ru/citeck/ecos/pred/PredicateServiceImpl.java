package ru.citeck.ecos.pred;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.PredicateModel;
import ru.citeck.ecos.utils.DictionaryUtils;


class PredicateServiceImpl implements PredicateService {
    
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private Map<QName, PredicateEvaluator> evaluators;
    
    /*
     * Spring interface
     */

    public PredicateServiceImpl() {
        this.evaluators = new HashMap<>();
    }
    
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
    
    /*package*/ void registerEvaluator(PredicateEvaluator evaluator) {
        evaluators.put(evaluator.getPredicateType(), evaluator);
    }

    /*
     * PredicateService interface
     */

    @Override
    public boolean evaluatePredicate(NodeRef predicate, Map<String, Object> model) {
        PredicateEvaluator evaluator = needEvaluator(predicate);
        return evaluator.evaluate(predicate, model);
    }
    
    @Override
    public Quantifier getQuantifier(NodeRef requirement) {
        String reqQuantifier = (String) nodeService.getProperty(requirement, PredicateModel.PROP_QUANTIFIER);
        return Quantifier.valueOf(reqQuantifier.toString());
    }
    
    @Override
    public boolean evaluateQuantifier(Quantifier quantifier, Collection<?> matchingElements) {
        return quantifier.evaluate(matchingElements);
    }

    @Override
    public boolean evaluateQuantifier(NodeRef quantifiable, List<?> matchingElements) {
        return evaluateQuantifier(getQuantifier(quantifiable), matchingElements);
    }
    
    /*
     * Private staff
     */

    private PredicateEvaluator getEvaluator(NodeRef predicate) {
        List<QName> predicateTypes = DictionaryUtils.getAllNodeTypeNames(predicate, nodeService, dictionaryService);
        for(QName predicateType : predicateTypes) {
            PredicateEvaluator evaluator = evaluators.get(predicateType);
            if(evaluator != null) {
                return evaluator;
            }
        }
        return null;
    }
    
    private PredicateEvaluator needEvaluator(NodeRef predicate) {
        PredicateEvaluator evaluator = getEvaluator(predicate);
        if(evaluator != null) return evaluator;
        throw new IllegalStateException("Evaluator for node " + predicate + " (type " + nodeService.getType(predicate) + ") is not registered");
    }

}
