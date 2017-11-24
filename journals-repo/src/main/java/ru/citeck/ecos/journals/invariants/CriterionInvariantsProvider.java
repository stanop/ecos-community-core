package ru.citeck.ecos.journals.invariants;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.JournalType;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CriterionInvariantsProvider {

    private static final String TYPE_OPTION = "type";

    private Map<Pair<String, QName>, List<InvariantDefinition>> cache = new ConcurrentHashMap<>();

    boolean enableCache = true;

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;

    public List<InvariantDefinition> getInvariants(JournalType journalType, QName attribute) {

        if (enableCache) {
            Pair<String, QName> key = new Pair<>(journalType.getId(), attribute);
            return cache.computeIfAbsent(key, k -> getInvariantsInternal(journalType, attribute));
        } else {
            return getInvariantsInternal(journalType, attribute);
        }
    }

    @PostConstruct
    public void init() {
        beforeInitImpl();
    }

    private List<InvariantDefinition> getInvariantsInternal(JournalType journalType, QName attribute) {
        String typeOpt = journalType.getOptions().get(TYPE_OPTION);
        QName typeParam = typeOpt != null ? QName.resolveToQName(namespaceService, typeOpt) : null;
        return getInvariantsImpl(journalType, typeParam, attribute);
    }

    protected void beforeInitImpl() {}
    protected abstract List<InvariantDefinition> getInvariantsImpl(JournalType journalType, QName typeName, QName attribute);

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.nodeService = serviceRegistry.getNodeService();
    }
}
