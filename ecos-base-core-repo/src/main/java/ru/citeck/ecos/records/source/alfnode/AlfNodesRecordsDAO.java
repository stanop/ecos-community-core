package ru.citeck.ecos.records.source.alfnode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordMut;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;
import ru.citeck.ecos.records.source.*;
import ru.citeck.ecos.records.source.alfnode.meta.AlfNodeRecord;
import ru.citeck.ecos.records.source.alfnode.search.AlfNodesSearch;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AlfNodesRecordsDAO extends LocalRecordsDAO
                                implements RecordsDefinitionDAO,
                                           RecordsMetaDAO,
                                           MutableRecordsDAO {

    private static final Log logger = LogFactory.getLog(AlfNodesRecordsDAO.class);

    public static final String ID = "";

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    private SearchService searchService;
    private NodeService nodeService;

    private Map<QName, NodeRef> defaultParentByType = new ConcurrentHashMap<>();

    public AlfNodesRecordsDAO() {
        setId(ID);
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutResult result = new RecordsMutResult();

        for (RecordMut record : mutation.getRecords()) {

            Map<QName, Serializable> props = new HashMap<>();

            ObjectNode fields = record.getAttributes();
            Iterator<String> names = fields.fieldNames();
            while (names.hasNext()) {

                String name = names.next();
                QName fielName = QName.resolveToQName(namespaceService, name);

                props.put(fielName, fields.path(name).asText());
            }

            if (record.getId() == null) {

                QName type = getNodeType(record);
                NodeRef parent = getParent(record, type);
                QName parentAssoc = getParentAssoc(record, parent);

                String name = (String) props.get(ContentModel.PROP_NAME);
                if (StringUtils.isBlank(name)) {
                    name = GUID.generate();
                }
                QName assocName = QName.createQName(parentAssoc.getNamespaceURI(), name);

                ChildAssociationRef child = nodeService.createNode(parent, parentAssoc, assocName, type, props);
                result.add(new RecordRef(child.getChildRef()));

            } else {

                NodeRef nodeRef = new NodeRef(record.getId().getId());
                nodeService.addProperties(nodeRef, props);
                result.add(record.getId());
            }
        }

        return result;
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        for (RecordRef recordRef : deletion.getRecords()) {
            nodeService.deleteNode(new NodeRef(recordRef.getId()));
        }
        return new RecordsDelResult();
    }

    private QName getParentAssoc(RecordMut record, NodeRef parentRef) {
        String parentAtt = record.getParentAtt();
        if (parentAtt != null) {
            return QName.resolveToQName(namespaceService, parentAtt);
        }
        QName parentType = nodeService.getType(parentRef);
        if (ContentModel.TYPE_CONTAINER.equals(parentType)) {
            return ContentModel.ASSOC_CHILDREN;
        } else if (ContentModel.TYPE_CATEGORY.equals(parentType)) {
            return ContentModel.ASSOC_SUBCATEGORIES;
        }
        return ContentModel.ASSOC_CONTAINS;
    }

    private QName getNodeType(RecordMut record) {

        QName typeQName;

        String type = record.getType();
        if (type != null) {
            typeQName = QName.resolveToQName(namespaceService, type);
        } else {
            typeQName = ContentModel.TYPE_CONTENT;
        }
        if (typeQName == null) {
            throw new IllegalArgumentException("Incorrect type: " + record.getType());
        }

        return typeQName;
    }

    private NodeRef getParent(RecordMut record, QName type) {

        String parent = record.getParent();
        if (parent != null) {
            if (parent.startsWith("workspace")) {
                return new NodeRef(parent);
            }
            return getByPath(parent);
        }

        NodeRef parentRef = defaultParentByType.get(type);
        if (parentRef != null) {
            return parentRef;
        }

        ClassDefinition typeDef = dictionaryService.getType(type);
        typeDef = typeDef.getParentClassDefinition();

        while (typeDef != null) {
            parentRef = defaultParentByType.get(typeDef.getName());
            if (parentRef != null) {
                return parentRef;
            }
            typeDef = typeDef.getParentClassDefinition();
        }

        return new NodeRef("workspace://SpacesStore/attachments-root");
    }

    private NodeRef getByPath(String path) {

        NodeRef root = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(root, path, null,
                                                          namespaceService, false);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Node not found by path: " + path);
        }
        return results.get(0);
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
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    public void register(AlfNodesSearch alfNodesSearch) {
        searchByLanguage.put(alfNodesSearch.getLanguage(), alfNodesSearch);
    }

    public void registerDefaultParentByType(Map<QName, NodeRef> defaultParentByType) {
        this.defaultParentByType.putAll(defaultParentByType);
    }
}
