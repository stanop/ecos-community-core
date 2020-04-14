package ru.citeck.ecos.records.source.alf;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.commons.data.DataValue;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.model.InvariantsModel;
import ru.citeck.ecos.records.source.alf.file.AlfNodeContentFileHelper;
import ru.citeck.ecos.records.source.alf.meta.AlfNodeRecord;
import ru.citeck.ecos.records.source.alf.search.AlfNodesSearch;
import ru.citeck.ecos.records.source.dao.RecordsActionExecutor;
import ru.citeck.ecos.records2.RecordConstants;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.citeck.ecos.model.ClassificationModel.PROP_DOCUMENT_KIND;
import static ru.citeck.ecos.model.ClassificationModel.PROP_DOCUMENT_TYPE;

@Component
@Slf4j
public class AlfNodesRecordsDAO extends LocalRecordsDAO
    implements RecordsQueryDAO,
    RecordsMetaLocalDAO<MetaValue>,
    RecordsQueryWithMetaLocalDAO<Object>,
    MutableRecordsDAO, RecordsActionExecutor {

    public static final String ID = "";
    private static final String ADD_CMD_PREFIX = "att_add_";
    private static final String REMOVE_CMD_PREFIX = "att_rem_";
    private static final String ETYPE_ATTRIBUTE_NAME = "_etype";
    private static final String SLASH_DELIMITER = "/";
    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";
    private static final String STATE_ATTRIBUTE_NAME = "_state";
    private static final String CONTENT_ATTRIBUTE_NAME = "_content";
    private static final String CM_CONTENT_ATTRIBUTE_NAME = "cm:content";

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    private NodeUtils nodeUtils;
    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private GroupActionService groupActionService;
    private AlfNodeContentFileHelper contentFileHelper;
    private EcosPermissionService ecosPermissionService;

    private Map<QName, NodeRef> defaultParentByType = new ConcurrentHashMap<>();

    public AlfNodesRecordsDAO() {
        setId(ID);
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutResult result = new RecordsMutResult();

        for (RecordMeta record : mutation.getRecords()) {
            RecordMeta resRec = processSingleRecord(record);
            result.addRecord(resRec);
        }

        return result;
    }

    private RecordMeta processSingleRecord(RecordMeta record) {

        RecordMeta resultRecord;
        Map<QName, Serializable> props = new HashMap<>();
        Map<QName, DataValue> contentProps = new HashMap<>();
        Map<QName, Set<NodeRef>> assocs = new HashMap<>();
        Map<QName, DataValue> childAssocEformFiles = new HashMap<>();
        Map<QName, DataValue> attachmentAssocEformFiles = new HashMap<>();
        Map<String, String> attsToIgnore = new HashMap<>();

        NodeRef nodeRef = null;
        if (record.getId().getId().startsWith("workspace://SpacesStore/")) {
            nodeRef = new NodeRef(record.getId().getId());
        }

        ObjectData attributes = record.getAttributes();

        // if we get "att_add_someAtt" and "someAtt", then ignore "att_add_*"
        attributes.forEach((name, value) -> {

            if (name.startsWith(ADD_CMD_PREFIX) || name.startsWith(REMOVE_CMD_PREFIX)) {

                String attrNameWithoutPrefix;
                if (name.startsWith(ADD_CMD_PREFIX)) {
                    attrNameWithoutPrefix = name.replaceFirst(ADD_CMD_PREFIX, StringUtils.EMPTY);
                } else {
                    attrNameWithoutPrefix = name.replaceFirst(REMOVE_CMD_PREFIX, StringUtils.EMPTY);
                }

                if (attributes.has(attrNameWithoutPrefix) && value.isNotNull()) {
                    String actualName = extractActualAttName(name);
                    attsToIgnore.put(name, actualName);
                }
            }
        });

        handleContentAttribute(attributes);
        handleETypeAttribute(attributes, props);

        Iterator<String> names = attributes.fieldNames();
        while (names.hasNext()) {

            String name = names.next();
            DataValue fieldValue = attributes.get(name);
            DataValue fieldRawValue = attributes.get(name);

            if (attsToIgnore.containsKey(name)) {
                log.warn("Found att " + attsToIgnore.get(name) + ", att " + name + " will be ignored");
                continue;
            }

            if (STATE_ATTRIBUTE_NAME.equals(name)) {
                String strValue = fieldValue.asText();
                props.put(InvariantsModel.PROP_IS_DRAFT, "draft".equals(strValue));
                continue;
            }

            String addOrRemoveCmd = null;
            if (record.getId() != RecordRef.EMPTY && name.startsWith(ADD_CMD_PREFIX)) {
                addOrRemoveCmd = ADD_CMD_PREFIX;
                name = name.substring(ADD_CMD_PREFIX.length());
                if (!name.contains(":")) {
                    log.warn("Attribute doesn't exist: " + name);
                    continue;
                }
            } else if (record.getId() != RecordRef.EMPTY && name.startsWith(REMOVE_CMD_PREFIX)) {
                addOrRemoveCmd = REMOVE_CMD_PREFIX;
                name = name.substring(REMOVE_CMD_PREFIX.length());
                if (!name.contains(":")) {
                    log.warn("Attribute doesn't exist: " + name);
                    continue;
                }
            }

            if (ecosPermissionService.isAttributeProtected(nodeRef, name)) {
                log.warn("You can't change '" + name +
                    "' attribute of '" + nodeRef +
                    "' because it is protected! Value: " + fieldRawValue);
                continue;
            }

            QName fieldName = QName.resolveToQName(namespaceService, name);
            if (fieldName == null) {
                continue;
            }

            PropertyDefinition propDef = dictionaryService.getProperty(fieldName);

            if (propDef != null) {

                QName typeName = propDef.getDataType().getName();
                if (addOrRemoveCmd != null) {
                    log.warn("Attribute action " + addOrRemoveCmd + " is not supported for node properties." +
                        " Atttribute: " + name);
                    continue;
                }

                if (DataTypeDefinition.CONTENT.equals(typeName)) {
                    contentProps.put(fieldName, fieldValue);
                } else {

                    Object converted = fieldValue.asJavaObj();

                    if (converted != null) {
                        if (!DataTypeDefinition.TEXT.equals(typeName)
                                && converted instanceof String
                                && ((String) converted).isEmpty()) {
                            converted = null;
                        }
                        if (!(converted instanceof Serializable)) {
                            converted = null;
                        }
                    }

                    props.put(fieldName, (Serializable) converted);
                }
            } else {
                AssociationDefinition assocDef = dictionaryService.getAssociation(fieldName);
                if (assocDef != null) {

                    if (contentFileHelper.isFileFromEformFormat(fieldValue)) {
                        if (addOrRemoveCmd != null) {
                            log.warn("Attribute action " + addOrRemoveCmd + " is not supported for fileFromEformFormat." +
                                " Atttribute: " + name);
                            continue;
                        }
                        if (assocDef instanceof ChildAssociationDefinition) {
                            childAssocEformFiles.put(fieldName, fieldValue);
                        } else {
                            attachmentAssocEformFiles.put(fieldName, fieldValue);
                        }
                    } else {
                        Stream<String> refsStream = null;
                        if (fieldValue.isTextual()) {
                            refsStream = Arrays.stream(fieldValue.asText().split(","));
                        } else if (fieldValue.isArray()) {
                            refsStream = StreamSupport.stream(fieldValue.spliterator(), false)
                                .map(DataValue::asText);
                        } else if (fieldValue.isNull()) {
                            refsStream = Stream.empty();
                        }
                        if (refsStream != null) {
                            Set<NodeRef> targetRefs = refsStream
                                .filter(NodeRef::isNodeRef)
                                .map(NodeRef::new)
                                .collect(Collectors.toSet());

                            if (!targetRefs.isEmpty() && addOrRemoveCmd != null) {
                                Set<NodeRef> existedAssocTargets = assocs.get(fieldName);
                                if (existedAssocTargets == null) {
                                    existedAssocTargets = new HashSet<>(nodeUtils.getAssocTargets(nodeRef, fieldName));
                                }
                                if (ADD_CMD_PREFIX.equals(addOrRemoveCmd)) {
                                    existedAssocTargets.addAll(targetRefs);
                                }

                                if (REMOVE_CMD_PREFIX.equals(addOrRemoveCmd)) {
                                    existedAssocTargets.removeAll(targetRefs);
                                }
                                targetRefs = existedAssocTargets;
                            }
                            assocs.put(fieldName, targetRefs);
                        }
                    }
                }
            }
        }

        if (record.getId() == RecordRef.EMPTY) {

            if (!props.containsKey(InvariantsModel.PROP_IS_DRAFT)) {
                props.put(InvariantsModel.PROP_IS_DRAFT, false);
            }

            QName type = getNodeType(record);
            NodeRef parent = getParent(record, type);
            QName parentAssoc = getParentAssoc(record, parent);

            String name = (String) props.get(ContentModel.PROP_NAME);

            if (StringUtils.isBlank(name)) {

                DataValue content = contentProps.get(ContentModel.PROP_CONTENT);

                if (content != null && content.isObject()) {
                    DataValue contentName = content.get("name");
                    if (!contentName.isTextual()) {
                        contentName = content.get("filename");
                    }
                    if (contentName.isTextual()) {
                        name = contentName.asText();
                    }
                }
                if (StringUtils.isBlank(name)) {
                    name = GUID.generate();
                }
            }

            props.put(ContentModel.PROP_NAME, name);

            nodeRef = nodeUtils.createNode(parent, type, parentAssoc, props);
            resultRecord = new RecordMeta(RecordRef.valueOf(nodeRef.toString()));

        } else {

            Map<QName, Serializable> currentProps = nodeService.getProperties(nodeRef);

            List<QName> toRemove = new ArrayList<>();
            for (Map.Entry<QName, Serializable> keyValue : props.entrySet()) {
                if (keyValue.getValue() == null && currentProps.get(keyValue.getKey()) == null) {
                    toRemove.add(keyValue.getKey());
                }
            }
            toRemove.forEach(props::remove);

            if (props.size() > 0) {
                nodeService.addProperties(nodeRef, props);
            }
            resultRecord = new RecordMeta(record.getId());
        }

        final NodeRef finalNodeRef = nodeRef;

        contentProps.forEach((name, value) -> contentFileHelper.processPropFileContent(finalNodeRef, name, value));
        assocs.forEach((name, value) -> nodeUtils.setAssocs(finalNodeRef, value, name, true));
        childAssocEformFiles.forEach((qName, jsonNodes) -> contentFileHelper.processAssocFilesContent(
            qName, jsonNodes, finalNodeRef, true));
        attachmentAssocEformFiles.forEach((qName, jsonNodes) -> contentFileHelper.processAssocFilesContent(
            qName, jsonNodes, finalNodeRef, false));
        return resultRecord;
    }

    private void handleETypeAttribute(ObjectData attributes, Map<QName, Serializable> props) {

        DataValue attributeFieldValue = attributes.get(ETYPE_ATTRIBUTE_NAME);
        if (!attributeFieldValue.isNull()) {

            String attrValue = attributeFieldValue.asText();
            if (!StringUtils.isBlank(attrValue)) {

                String typeId = RecordRef.valueOf(attrValue).getId();
                int slashIndex = typeId.indexOf(SLASH_DELIMITER);
                if (slashIndex != -1) {

                    String firstPartOfTypeId = typeId.substring(0, slashIndex);
                    props.put(PROP_DOCUMENT_TYPE, WORKSPACE_PREFIX + firstPartOfTypeId);

                    String secondPartOfRecordId = typeId.substring(slashIndex + 1);
                    props.put(PROP_DOCUMENT_KIND, WORKSPACE_PREFIX + secondPartOfRecordId);

                } else {
                    props.put(PROP_DOCUMENT_TYPE, WORKSPACE_PREFIX + typeId);
                }
            }

            attributes.remove(ETYPE_ATTRIBUTE_NAME);
        }
    }

    private void handleContentAttribute(ObjectData attributes) {

        DataValue attributeFieldValue = attributes.get(CONTENT_ATTRIBUTE_NAME);
        if (!attributeFieldValue.isNull()) {
            attributes.remove(CONTENT_ATTRIBUTE_NAME);
            attributes.set(CM_CONTENT_ATTRIBUTE_NAME, attributeFieldValue);
        }
    }

    private String extractActualAttName(String name) {
        if (name.startsWith(ADD_CMD_PREFIX)) {
            return name.substring(ADD_CMD_PREFIX.length());
        } else if (name.startsWith(REMOVE_CMD_PREFIX)) {
            return name.substring(REMOVE_CMD_PREFIX.length());
        } else {
            return name;
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Query records with query: " + query +
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

    @Autowired
    public void setContentFileHelper(AlfNodeContentFileHelper contentFileHelper) {
        this.contentFileHelper = contentFileHelper;
    }
}
