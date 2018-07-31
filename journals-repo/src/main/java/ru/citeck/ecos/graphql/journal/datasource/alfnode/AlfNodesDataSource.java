package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import com.google.common.collect.Lists;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.search.AlfNodesSearch;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AlfNodesDataSource implements JournalDataSource {

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    private Map<String, AlfNodesSearch> nodesSearchByLang = new ConcurrentHashMap<>();

    @Autowired
    public AlfNodesDataSource(ServiceRegistry serviceRegistry) {

        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    @Override
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {

        AlfNodesSearch alfNodesSearch = nodesSearchByLang.get(language);

        if (alfNodesSearch == null) {
            throw new IllegalArgumentException("Language " + language + " is not supported!");
        }

        return alfNodesSearch.query(context, query, pageInfo);
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.of(new AlfNodeAttributeInfo(attributeName, namespaceService, dictionaryService));
    }

    @Override
    public List<String> getDefaultAttributes() {
        return Lists.newArrayList(AlfNodeRecord.ATTR_ASPECTS,
                                  AlfNodeRecord.ATTR_IS_DOCUMENT,
                                  AlfNodeRecord.ATTR_IS_CONTAINER);
    }

    public void register(AlfNodesSearch nodesSearch) {
        nodesSearchByLang.put(nodesSearch.getLanguage(), nodesSearch);
    }
}
