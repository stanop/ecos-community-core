package ru.citeck.ecos.pred;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


public interface PredicateService {

    boolean evaluatePredicate(NodeRef predicate, Map<String, Object> model);
    
    Quantifier getQuantifier(NodeRef requirement);
    
    boolean evaluateQuantifier(Quantifier quantifier, Collection<?> matchingElements);
    
    boolean evaluateQuantifier(NodeRef quantifiable, List<?> matchingElements);
}
