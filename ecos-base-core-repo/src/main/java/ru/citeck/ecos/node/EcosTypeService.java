package ru.citeck.ecos.node;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.records2.RecordRef;

import java.util.function.Function;

@Service
public class EcosTypeService {

    public static final QName QNAME = QName.createQName("", "ecosTypeService");

    private EvaluatorsByAlfNode<RecordRef> evaluators;

    @Autowired
    public EcosTypeService(ServiceRegistry serviceRegistry) {
        evaluators = new EvaluatorsByAlfNode<>(serviceRegistry, node -> null);
    }

    public RecordRef getEcosType(NodeRef nodeRef) {
        return evaluators.eval(nodeRef);
    }

    public RecordRef getEcosType(AlfNodeInfo nodeInfo) {
        return evaluators.eval(nodeInfo);
    }

    public void register(QName nodeType, Function<AlfNodeInfo, RecordRef> evaluator) {
        evaluators.register(nodeType, evaluator);
    }
}
