package ru.citeck.ecos.journals.invariants;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.utils.DictUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CriterionInvariantsProvider implements Comparable<CriterionInvariantsProvider> {

    private static final String TYPE_OPTION = "type";

    private Map<Pair<String, QName>, List<InvariantDefinition>> cache = new ConcurrentHashMap<>();

    private boolean enableCache = true;
    private int order = 0;

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected JournalService journalService;
    protected DictUtils dictUtils;

    public final List<InvariantDefinition> getInvariants(JournalType journalType, QName attribute) {
        if (enableCache) {
            Pair<String, QName> key = new Pair<>(journalType.getId(), attribute);
            return cache.computeIfAbsent(key, k -> getInvariantsInternal(journalType, attribute));
        } else {
            return getInvariantsInternal(journalType, attribute);
        }
    }

    @PostConstruct
    public final void init() {
        beforeInitImpl();
        journalService.registerCriterionInvariantsProvider(this);
    }

    public void clearCache() {
        cache.clear();
    }

    private List<InvariantDefinition> getInvariantsInternal(JournalType journalType, QName attribute) {
        String typeOpt = journalType.getOptions().get(TYPE_OPTION);
        QName typeParam = typeOpt != null ? QName.resolveToQName(namespaceService, typeOpt) : null;
        if (isAttributeSupported(journalType, typeParam, attribute)) {
            return getInvariantsImpl(journalType, typeParam, attribute);
        }
        return Collections.emptyList();
    }

    protected void beforeInitImpl() {}
    protected abstract boolean isAttributeSupported(JournalType journalType, QName typeName, QName attribute);
    protected abstract List<InvariantDefinition> getInvariantsImpl(JournalType journalType, QName typeName, QName attribute);

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    @Autowired
    public void setDictUtils(DictUtils dictUtils) {
        this.dictUtils = dictUtils;
    }

    @Override
    public int compareTo(CriterionInvariantsProvider other) {
        return Integer.compare(order, other.order);
    }
}
