package ru.citeck.ecos.node;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AlfNodeContentPathRegistry {

    public static final QName QNAME = QName.createQName("", "alfNodeContentPathRegistry");

    private EvaluatorsByAlfNode<String> evaluators;

    @Autowired
    public AlfNodeContentPathRegistry(ServiceRegistry serviceRegistry) {
        evaluators = new EvaluatorsByAlfNode<>(serviceRegistry, node -> "cm:content");
    }

    public String getContentPath(NodeRef nodeRef) {
        return evaluators.eval(nodeRef);
    }

    public String getContentPath(AlfNodeInfo nodeInfo) {
        return evaluators.eval(nodeInfo);
    }

    public void register(QName nodeType, Function<AlfNodeInfo, String> evaluator) {
        evaluators.register(nodeType, evaluator);
    }
}
