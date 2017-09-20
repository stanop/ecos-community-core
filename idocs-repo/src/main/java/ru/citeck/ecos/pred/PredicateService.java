package ru.citeck.ecos.pred;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


public interface PredicateService {

    public boolean evaluatePredicate(NodeRef predicate, Map<String, Object> model);
    
    public Quantifier getQuantifier(NodeRef requirement);
    
    public boolean evaluateQuantifier(Quantifier quantifier, Collection<?> matchingElements);
    
    public boolean evaluateQuantifier(NodeRef quantifiable, List<?> matchingElements);
}
