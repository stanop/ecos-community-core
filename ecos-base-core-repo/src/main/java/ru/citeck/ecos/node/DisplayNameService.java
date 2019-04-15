package ru.citeck.ecos.node;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class DisplayNameService {

    public static final QName QNAME = QName.createQName("", "displayNameService");

    private ServiceRegistry serviceRegistry;
    private DictionaryService dictionaryService;

    private Map<QName, Function<AlfNodeInfo, String>> evaluators = new ConcurrentHashMap<>();
    private Map<QName, List<Function<AlfNodeInfo, String>>> typeEvaluators = new ConcurrentHashMap<>();

    public String getDisplayName(NodeRef nodeRef) {
        return getDisplayName(new AlfNodeInfoImpl(nodeRef, serviceRegistry));
    }

    public String getDisplayName(AlfNodeInfo nodeInfo) {

        QName type = nodeInfo.getType();

        List<Function<AlfNodeInfo, String>> evaluators = getEvaluatorsForType(type);
        String name = null;
        int i = 0;
        while (name == null && i < evaluators.size()) {
            name = evaluators.get(i).apply(nodeInfo);
            i++;
        }

        if (name == null) {
            name = String.valueOf(nodeInfo.getNodeRef());
        }

        return name;
    }

    private List<Function<AlfNodeInfo, String>> getEvaluatorsForType(QName type) {

        return typeEvaluators.computeIfAbsent(type, t -> {

            List<Function<AlfNodeInfo, String>> result = new ArrayList<>();

            ClassDefinition typeDef = dictionaryService.getClass(type);
            while (typeDef != null) {
                Function<AlfNodeInfo, String> evaluator = evaluators.get(typeDef.getName());
                if (evaluator != null) {
                    result.add(evaluator);
                }
                typeDef = typeDef.getParentClassDefinition();
            }

            return result;
        });
    }

    public void register(QName nodeType, Function<AlfNodeInfo, String> evaluator) {
        evaluators.put(nodeType, evaluator);
        typeEvaluators.clear();
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }
}
