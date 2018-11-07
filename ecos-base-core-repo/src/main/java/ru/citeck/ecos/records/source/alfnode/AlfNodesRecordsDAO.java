package ru.citeck.ecos.records.source.alfnode;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.*;
import ru.citeck.ecos.records.source.alfnode.meta.AlfNodeRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AlfNodesRecordsDAO extends LocalRecordsDAO
                                implements RecordsDefinitionDAO,
                                           RecordsMetaDAO {

    private static final Log logger = LogFactory.getLog(AlfNodesRecordsDAO.class);

    public static final String ID = "";

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    private NodeService nodeService;

    public AlfNodesRecordsDAO() {
        setId(ID);
    }

    @Override
    public RecordsResult<RecordRef> getRecords(RecordsQuery query) {
        AlfNodesSearch alfNodesSearch = searchByLanguage.get(query.getLanguage());
        if (alfNodesSearch == null) {
            throw new IllegalArgumentException("Language " + query.getLanguage() +
                                               " is not supported! Query: " + query);
        }
        Long afterIdValue = null;
        Date afterCreated = null;
        if (query.isAfterIdMode()) {

            RecordRef afterId = query.getAfterId();

            AlfNodesSearch.AfterIdType afterIdType = alfNodesSearch.getAfterIdType();

            if (afterId != null) {
                if (!ID.equals(afterId.getSourceId())) {
                    return new RecordsResult<>();
                }
                NodeRef afterIdNodeRef = new NodeRef(afterId.getId());

                if (afterIdType == null) {
                    throw new IllegalArgumentException("Page parameter afterId is not supported " +
                                                       "by language " + query.getLanguage() + ". query: " + query);
                }
                switch (afterIdType) {
                    case DB_ID:
                        afterIdValue = (Long) nodeService.getProperty(afterIdNodeRef, ContentModel.PROP_NODE_DBID);
                        break;
                    case CREATED:
                        afterCreated = (Date) nodeService.getProperty(afterIdNodeRef, ContentModel.PROP_CREATED);
                        break;
                }
            } else {
                switch (afterIdType) {
                    case DB_ID:
                        afterIdValue = 0L;
                        break;
                    case CREATED:
                        afterCreated = new Date(0);
                        break;
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Query records with query: " + query +
                         " afterIdValue: " + afterIdValue + " afterCreated: " + afterCreated);
        }
        return alfNodesSearch.queryRecords(query, afterIdValue, afterCreated);
    }

    @Override
    public List<MetaValue> getMetaValues(GqlContext context, List<RecordRef> recordRef) {
        return recordRef.stream()
                        .map(RecordRef::getId)
                        .map(context::getNode)
                        .filter(Optional::isPresent)
                        .map(n -> new AlfNodeRecord(n.get(), context))
                        .collect(Collectors.toList());
    }

    @Override
    public List<MetaValueTypeDef> getTypesDefinition(Collection<String> names) {
        return Collections.emptyList();
    }

    @Override
    public List<MetaAttributeDef> getAttsDefinition(Collection<String> names) {
        return names.stream()
                    .map(n -> new AlfAttributeDefinition(n, namespaceService, dictionaryService, messageService))
                    .collect(Collectors.toList());
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.messageService = serviceRegistry.getMessageService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    public void register(AlfNodesSearch alfNodesSearch) {
        searchByLanguage.put(alfNodesSearch.getLanguage(), alfNodesSearch);
    }
}
