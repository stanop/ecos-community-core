package ru.citeck.ecos.node;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import java.util.List;
import java.util.function.Function;

@Service("ecosTypeService")
@Slf4j
public class EcosTypeService {

    public static final QName QNAME = QName.createQName("", "ecosTypeService");
    private static final RecordRef DEFAULT_TYPE = RecordRef.create("emodel", "type", "base");

    private EvaluatorsByAlfNode<RecordRef> evaluators;
    private RecordsService recordsService;

    @Autowired
    public EcosTypeService(ServiceRegistry serviceRegistry, RecordsService recordsService) {
        evaluators = new EvaluatorsByAlfNode<>(serviceRegistry, node -> DEFAULT_TYPE);
        this.recordsService = recordsService;
    }

    public RecordRef getEcosType(NodeRef nodeRef) {
        return evaluators.eval(nodeRef);
    }

    public RecordRef getEcosType(AlfNodeInfo nodeInfo) {
        return evaluators.eval(nodeInfo);
    }

    public void forEachAscRef(RecordRef typeRef, Function<RecordRef, Boolean> action) {

        if (action.apply(typeRef)) {
            return;
        }

        TypeParents typeInfo = recordsService.getMeta(typeRef, TypeParents.class);
        if (typeInfo == null || typeInfo.parents == null) {
            log.error("ECOS type parents can't be resolved");
            return;
        }

        for (RecordRef parentRef : typeInfo.parents) {
            if (action.apply(parentRef)) {
                return;
            }
        }
    }

    public void register(QName nodeType, Function<AlfNodeInfo, RecordRef> evaluator) {
        evaluators.register(nodeType, evaluator);
    }

    @Data
    public static class TypeParents {
        private List<RecordRef> parents;
    }
}
