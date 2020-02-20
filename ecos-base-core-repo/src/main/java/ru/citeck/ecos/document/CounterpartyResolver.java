package ru.citeck.ecos.document;

import lombok.NonNull;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.spring.registry.MappingRegistry;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Roman Makarskiy
 */
@Component
public class CounterpartyResolver {

    private final NodeService nodeService;
    private final NamespaceService namespaceService;
    private final DictionaryService dictionaryService;

    private MappingRegistry<String, String> documentToCounterparty;

    @Autowired
    public CounterpartyResolver(NodeService nodeService, NamespaceService namespaceService,
                                DictionaryService dictionaryService) {
        this.nodeService = nodeService;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
    }

    public NodeRef resolve(@NonNull NodeRef document) {
        QName documentType = nodeService.getType(document);

        Map<QName, QName> mapping = documentToCounterparty.getMapping().entrySet().stream().collect(Collectors.toMap(
            entry -> QName.resolveToQName(namespaceService, entry.getKey()),
            entry -> QName.resolveToQName(namespaceService, entry.getValue())
        ));

        if (mapping.containsKey(documentType)) {
            return RepoUtils.getFirstTargetAssoc(document, mapping.get(documentType), nodeService);
        }

        ClassDefinition classDefinition = dictionaryService.getClass(documentType);
        while (classDefinition != null) {
            classDefinition = classDefinition.getParentClassDefinition();
            if (classDefinition != null) {
                QName currentName = classDefinition.getName();
                if (mapping.containsKey(currentName)) {
                    QName assocType = mapping.get(currentName);
                    return RepoUtils.getFirstTargetAssoc(document, assocType, nodeService);
                }
            }
        }

        return null;
    }

    @Autowired
    @Qualifier("documentToCounterparty.mappingRegistry")
    public void setDocumentToCounterparty(MappingRegistry<String, String> documentToCounterparty) {
        this.documentToCounterparty = documentToCounterparty;
    }
}
