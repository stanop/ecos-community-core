package ru.citeck.ecos.pred;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import ru.citeck.ecos.model.PredicateModel;
import ru.citeck.ecos.utils.RepoUtils;

public class ConditionPredicateEvaluator extends AbstractPredicateEvaluator {

    @Override
    public boolean evaluate(NodeRef condition, Map<String, Object> model) {
        List<NodeRef> antecedents = RepoUtils.getChildrenByAssoc(condition, PredicateModel.ASSOC_ANTECEDENT, nodeService);
        for(NodeRef antecedent : antecedents) {
            if(!predicateService.evaluatePredicate(antecedent, model)) 
                return true;
        }
        
        List<NodeRef> consequents = RepoUtils.getChildrenByAssoc(condition, PredicateModel.ASSOC_CONSEQUENT, nodeService);
        for(NodeRef antecedent : consequents) {
            if(!predicateService.evaluatePredicate(antecedent, model)) 
                return false;
        }
        return true;
    }

}
