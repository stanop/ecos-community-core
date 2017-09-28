package ru.citeck.ecos.pred;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface PredicateEvaluator {
    
    public boolean evaluate(NodeRef predicate, Map<String, Object> model);
    
    public QName getPredicateType();
    
}
