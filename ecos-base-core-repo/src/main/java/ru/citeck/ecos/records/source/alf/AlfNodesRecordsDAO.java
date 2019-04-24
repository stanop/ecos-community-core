package ru.citeck.ecos.records.source.alf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.records2.RecordConstants;
import ru.citeck.ecos.records.source.alf.meta.AlfNodeRecord;
import ru.citeck.ecos.records.source.alf.search.AlfNodesSearch;
import ru.citeck.ecos.records.source.dao.RecordsActionExecutor;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.MutableRecordsDAO;
import ru.citeck.ecos.records2.source.dao.RecordsQueryDAO;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsMetaLocalDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsQueryWithMetaLocalDAO;
import ru.citeck.ecos.security.EcosPermissionService;
import ru.citeck.ecos.utils.NodeUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AlfNodesRecordsDAO extends LocalRecordsDAO
                                implements RecordsQueryDAO,
                                           RecordsMetaLocalDAO<MetaValue>,
                                           RecordsQueryWithMetaLocalDAO<Object>,
                                           MutableRecordsDAO,
                                           RecordsActionExecutor {

    private static final Log logger = LogFactory.getLog(AlfNodesRecordsDAO.class);

    public static final String ID = "";

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    private NodeUtils nodeUtils;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private GroupActionService groupActionService;
    private EcosPermissionService ecosPermissionService;

    private Map<QName, NodeRef> defaultParentByType = new ConcurrentHashMap<>();

    public AlfNodesRecordsDAO() {
        setId(ID);
    }

    private Serializable convertValue(JsonNode node) {

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isArray()) {

            ArrayList<Serializable> values = new ArrayList<>();

            for (JsonNode subNode : node) {
                values.add(convertValue(subNode));
            }

            return values;

        } else if (node.isNumber()) {

            return node.isIntegralNumber() ? node.asLong() : node.asDouble();

        } else if (node.isBoolean()) {

            return node.asBoolean();
        }

        return node.asText();
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutResult result = new RecordsMutResult();

        for (RecordMeta record : mutation.getRecords()) {

            Map<QName, Serializable> props = new HashMap<>();
            Map<QName, JsonNode> contentProps = new HashMap<>();
            Map<QName, Set<NodeRef>> assocs = new HashMap<>();

            NodeRef nodeRef = null;
            if (record.getId().getId().startsWith("workspace://SpacesStore/")) {
                nodeRef = new NodeRef(record.getId().getId());
            }

            ObjectNode fields = record.getAttributes();
            Iterator<String> names = fields.fieldNames();
            while (names.hasNext()) {

                String name = names.next();

                if (ecosPermissionService.isAttributeProtected(nodeRef, name)) {
                    logger.warn("You can't change '" + name +
                                "' attribute of '" + nodeRef +
                                "' because it is protected! Value: " + fields.get(name));
                    continue;
                }

                QName fieldName = QName.resolveToQName(namespaceService, name);

                PropertyDefinition propDef = dictionaryService.getProperty(fieldName);

                if (propDef != null) {

                    if (DataTypeDefinition.CONTENT.equals(propDef.getDataType().getName())) {
                        contentProps.put(fieldName, fields.path(name));
                    } else {

                        JsonNode value = fields.path(name);

                        if (!value.isMissingNode()) {

                            props.put(fieldName, convertValue(value));
                        }
                    }
                } else {

                    AssociationDefinition assocDef = dictionaryService.getAssociation(fieldName);

                    if (assocDef != null) {

                        Set<NodeRef> nodeRefs = Arrays.stream(fields.path(name).asText().split(","))
                                                      .filter(NodeRef::isNodeRef)
                                                      .map(NodeRef::new)
                                                      .collect(Collectors.toSet());

                        if (nodeRefs.size() > 0) {
                            assocs.put(fieldName, nodeRefs);
                        }
                    }
                }
            }

            if (record.getId() == RecordRef.EMPTY) {

                QName type = getNodeType(record);
                NodeRef parent = getParent(record, type);
                QName parentAssoc = getParentAssoc(record, parent);

                String name = (String) props.get(ContentModel.PROP_NAME);

                if (StringUtils.isBlank(name)) {

                    JsonNode contentProp = contentProps.get(ContentModel.PROP_CONTENT);
                    if (contentProp != null && contentProp.isObject()) {
                        JsonNode filenameProp = contentProp.path("filename");
                        if (filenameProp.isTextual()) {
                            name = filenameProp.asText();
                        }
                    }
                }

                if (StringUtils.isBlank(name)) {
                    name = GUID.generate();
                }

                props.put(ContentModel.PROP_NAME, name);

                nodeRef = nodeUtils.createNode(parent, type, parentAssoc, props);
                result.addRecord(new RecordMeta(RecordRef.valueOf(nodeRef.toString())));

            } else {

                nodeService.addProperties(nodeRef, props);
                result.addRecord(new RecordMeta(record.getId()));
            }

            final NodeRef finalNodeRef = nodeRef;

            contentProps.forEach((name, value) -> {

                ContentWriter writer = contentService.getWriter(finalNodeRef, name, true);

                if (value.isTextual()) {

                    writer.putContent(value.asText());

                } else if (value.isObject()) {

                    JsonNode mimetypeProp = value.path("mimetype");
                    String mimetype = mimetypeProp.isTextual() ? mimetypeProp.asText() : MimetypeMap.MIMETYPE_BINARY;
                    if (MimetypeMap.MIMETYPE_BINARY.equals(mimetype)) {
                        JsonNode filename = value.path("filename");
                        if (filename.isTextual()) {
                            mimetype = mimetypeService.guessMimetype(filename.asText());
                        }
                    }
                    writer.setMimetype(mimetype);

                    JsonNode encoding = value.path("encoding");
                    if (encoding.isTextual()) {
                        writer.setEncoding(encoding.asText());
                    } else {
                        writer.setEncoding("UTF-8");
                    }
                    JsonNode content = value.path("content");
                    if (content.isTextual()) {
                        writer.putContent(content.asText());
                    }
                }
            });

            assocs.forEach((name, value) -> nodeUtils.setAssocs(finalNodeRef, value, name));
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

    private QName getParentAssoc(RecordMeta record, NodeRef parentRef) {
        String parentAtt = record.getAttribute(RecordConstants.ATT_PARENT_ATT, "");
        if (!parentAtt.isEmpty()) {
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

    private QName getNodeType(RecordMeta record) {

        QName typeQName;

        String type = record.getAttribute(RecordConstants.ATT_TYPE, "");
        if (!type.isEmpty()) {
            typeQName = QName.resolveToQName(namespaceService, type);
        } else {
            typeQName = ContentModel.TYPE_CONTENT;
        }
        if (typeQName == null) {
            throw new IllegalArgumentException("Incorrect type: " + type);
        }

        return typeQName;
    }

    private NodeRef getParent(RecordMeta record, QName type) {

        String parent = record.getAttribute(RecordConstants.ATT_PARENT, "");
        if (!parent.isEmpty()) {
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
    public RecordsQueryResult<Object> getMetaValues(RecordsQuery recordsQuery) {

        RecordsQueryResult<RecordRef> records = queryRecords(recordsQuery);

        RecordsQueryResult<Object> result = new RecordsQueryResult<>();
        result.merge(records);
        result.setHasMore(records.getHasMore());
        result.setTotalCount(records.getTotalCount());
        result.setRecords((List) getMetaValues(records.getRecords()));

        if (recordsQuery.isDebug()) {
            result.setDebugInfo(getClass(), "query", recordsQuery.getQuery());
            result.setDebugInfo(getClass(), "language", recordsQuery.getLanguage());
        }

        return result;
    }

    @Override
    public RecordsQueryResult<RecordRef> queryRecords(RecordsQuery query) {

        if (query.getLanguage().isEmpty()) {
            query.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        }

        AlfNodesSearch alfNodesSearch = needNodesSearch(query.getLanguage());

        Long afterIdValue = null;
        Date afterCreated = null;
        if (query.isAfterIdMode()) {

            RecordRef afterId = query.getAfterId();

            AlfNodesSearch.AfterIdType afterIdType = alfNodesSearch.getAfterIdType();

            if (afterId != RecordRef.EMPTY) {
                if (!ID.equals(afterId.getSourceId())) {
                    return new RecordsQueryResult<>();
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
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(searchByLanguage.keySet());
    }

    @Override
    public List<MetaValue> getMetaValues(List<RecordRef> recordRef) {
        return recordRef.stream()
                        .map(this::createMetaValue)
                        .collect(Collectors.toList());
    }

    private MetaValue createMetaValue(RecordRef recordRef) {
        if (recordRef == RecordRef.EMPTY) {
            return new EmptyAlfNode();
        }
        return new AlfNodeRecord(recordRef);
    }

    public ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config) {
        return groupActionService.execute(records, config);
    }

    private AlfNodesSearch needNodesSearch(String language) {

        AlfNodesSearch alfNodesSearch = searchByLanguage.get(language);

        if (alfNodesSearch == null) {
            throw new IllegalArgumentException("Language '" + language + "' is not supported!");
        }

        return alfNodesSearch;
    }

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.mimetypeService = serviceRegistry.getMimetypeService();
        this.contentService = serviceRegistry.getContentService();
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Autowired
    public void setEcosPermissionService(EcosPermissionService ecosPermissionService) {
        this.ecosPermissionService = ecosPermissionService;
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    public void register(AlfNodesSearch alfNodesSearch) {
        searchByLanguage.put(alfNodesSearch.getLanguage(), alfNodesSearch);
    }

    public void registerDefaultParentByType(Map<QName, NodeRef> defaultParentByType) {
        this.defaultParentByType.putAll(defaultParentByType);
    }
}
