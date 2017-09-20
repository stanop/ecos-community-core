package ru.citeck.ecos.pred;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public abstract class AbstractPredicateEvaluator implements PredicateEvaluator {

    private QName predicateType;
    protected PredicateServiceImpl predicateService;
    protected NodeService nodeService;
    
    @Override
    public QName getPredicateType() {
        return predicateType;
    }

    public void setPredicateType(QName predicateType) {
        this.predicateType = predicateType;
    }

    public void setPredicateService(PredicateServiceImpl predicateService) {
        this.predicateService = predicateService;
    }
    
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void init() {
        predicateService.registerEvaluator(this);
    }

}
