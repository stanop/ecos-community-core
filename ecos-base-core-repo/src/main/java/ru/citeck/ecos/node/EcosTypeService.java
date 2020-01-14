package ru.citeck.ecos.node;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.records2.RecordRef;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

@Service("ecosTypeService")
@Slf4j
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

    public RecordRef evalDefaultEcosType(AlfNodeInfo info) {

        Map<QName, Serializable> props = info.getProperties();

        NodeRef type = (NodeRef) props.get(ClassificationModel.PROP_DOCUMENT_TYPE);

        if (type == null) {
            log.warn("Type property of nodeRef is null");
            return null;
        }

        String ecosType = type.getId();

        NodeRef kind = (NodeRef) props.get(ClassificationModel.PROP_DOCUMENT_KIND);
        if (kind != null) {
            ecosType = ecosType + "/" + kind.getId();
        }

        return RecordRef.create("emodel", "type", ecosType);
    }
}
