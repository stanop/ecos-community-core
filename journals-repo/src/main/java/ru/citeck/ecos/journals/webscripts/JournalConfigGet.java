package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.Setter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.journals.JournalBatchEdit;
import ru.citeck.ecos.journals.JournalGroupAction;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.records.source.alf.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.server.utils.Utils;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Pavel Simonov
 */
public class JournalConfigGet extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(JournalConfigGet.class);

    private static final String PARAM_JOURNAL = "journalId";

    private ObjectMapper objectMapper = new ObjectMapper();

    private LoadingCache<String, JournalRepoData> journalRefById;

    private NodeUtils nodeUtils;
    private NodeService nodeService;
    private SearchService searchService;
    private MessageService messageService;
    private RecordsService recordsService;
    private JournalService journalService;
    private ServiceRegistry serviceRegistry;
    private TemplateService templateService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private CreateVariantsGet createVariantsGet;

    public JournalConfigGet() {
        journalRefById = CacheBuilder.newBuilder()
                                     .expireAfterWrite(600, TimeUnit.SECONDS)
                                     .maximumSize(200)
                                     .build(CacheLoader.from(this::getJournalRepoData));
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL);

        if (StringUtils.isBlank(journalId)) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "journalId is a mandatory parameter!");
        }

        JournalType journalType;

        if (journalId.startsWith("alf_")) {
            QName typeQName = QName.resolveToQName(namespaceService, journalId.substring(4));
            journalType = journalService.getJournalForType(typeQName).orElse(null);
        } else {
            if (NodeRef.isNodeRef(journalId)) {
                journalId = nodeUtils.getProperty(new NodeRef(journalId), JournalsModel.PROP_JOURNAL_TYPE);
            }
            journalType = journalService.getJournalType(journalId);
        }

        if (journalType == null) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Journal with id '" + journalId + "' not found!");
        }

        List<String> attributes = journalType.getAttributes();
        List<Column> columns = new ArrayList<>();
        String sourceId = journalType.getDataSource();
        if (sourceId == null) {
            sourceId = "";
        }

        Map<String, AttInfo> columnInfo = getAttributesInfo(sourceId, attributes);
        for (String name : attributes) {

            Column column = new Column();

            column.setDefault(journalType.isAttributeDefault(name));
            column.setGroupable(journalType.isAttributeGroupable(name));
            column.setSearchable(journalType.isAttributeSearchable(name));
            column.setSortable(journalType.isAttributeSortable(name));
            column.setVisible(journalType.isAttributeVisible(name));
            column.setAttribute(name);
            column.setParams(journalType.getAttributeOptions(name));
            column.setText(getColumnLabel(column));

            if (column.getParams() != null) {
                String schema = column.getParams().get("schema");
                if (StringUtils.isNotBlank(schema)) {
                    column.setSchema(schema);
                }
            }

            AttInfo info = columnInfo.get(name);
            column.setJavaClass(info.getJavaClass() != null ? info.getJavaClass().getName() : null);
            column.setEditorKey(info.getEditorKey());
            column.setType(info.getType());

            columns.add(column);
        }

        Response response = new Response();
        response.setId(journalType.getId());
        response.setColumns(columns);
        response.setMeta(getJournalMeta(journalType));
        response.setSourceId(sourceId);
        response.setParams(journalType.getOptions());

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), response);
        res.setStatus(Status.STATUS_OK);
    }

    private String getColumnLabel(Column column) {

        Map<String, String> params = column.getParams();
        if (params != null) {
            String custom = params.get("customLabel");
            if (custom != null) {
                String label = I18NUtil.getMessage(custom);
                return label != null ? label : custom;
            }
        }

        if (column.getAttribute().contains(":")) {

            QName attQName = QName.resolveToQName(namespaceService, column.getAttribute());

            if (attQName != null) {

                ClassAttributeDefinition attDef = dictionaryService.getProperty(attQName);

                if (attDef == null) {
                    attDef = dictionaryService.getAssociation(attQName);
                }

                if (attDef != null) {

                    String title = attDef.getTitle(messageService);
                    if (StringUtils.isNotBlank(title)) {
                        return title;
                    }
                }
            }
        }

        return column.getAttribute();
    }

    private JournalMeta getJournalMeta(JournalType journal) {

        JournalRepoData journalData = journalRefById.getUnchecked(journal.getId());

        JournalMeta meta = new JournalMeta();
        if (journalData.getNodeRef() == null) {
            return meta;
        }

        NodeRef journalRef = journalData.getNodeRef();
        meta.setNodeRef(String.valueOf(journalRef));
        meta.setTitle(nodeUtils.getDisplayName(journalRef));
        meta.setGroupActions(getGroupActions(journal));

        List<Criterion> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(namespaceService);

        for (NodeRef criterionRef : journalData.getCriteria()) {

            Map<QName, Serializable> props = nodeService.getProperties(criterionRef);

            Criterion criterion = new Criterion();
            criterion.setField(toPrefix((QName) props.get(JournalsModel.PROP_FIELD_QNAME)));
            criterion.setPredicate((String) props.get(JournalsModel.PROP_PREDICATE));
            criterion.setValue(getCriterionValue((String) props.get(JournalsModel.PROP_CRITERION_VALUE)));

            criteria.addCriteriaTriplet(criterion.getField(), criterion.getPredicate(), criterion.getValue());

            criteriaList.add(criterion);
        }

        if (StringUtils.isNotBlank(journal.getPredicate())) {

            try {
                meta.setPredicate(objectMapper.readTree(journal.getPredicate()));
            } catch (IOException e) {
                logger.error("Predicate is invalid: " + journal.getPredicate(), e);
            }
        }

        if (meta.getPredicate() == null) {

            JsonNode criteriaJson = objectMapper.valueToTree(criteria);
            try {
                meta.setPredicate(recordsService.convertQueryLanguage(criteriaJson,
                                                                      CriteriaAlfNodesSearch.LANGUAGE,
                                                                      RecordsService.LANGUAGE_PREDICATE));
            } catch (Exception e) {
                logger.error("Language conversion error. criteria: " + criteriaJson, e);
            }
        }

        try {
            if (StringUtils.isNotBlank(journal.getGroupBy())) {
                meta.setGroupBy(objectMapper.readTree(journal.getGroupBy()));
            }
        } catch (IOException e) {
            logger.error("GroupBy is invalid: " + journal.getGroupBy(), e);
        }

        meta.setCreateVariants(createVariantsGet.getVariantsByJournalId(journal.getId(), true));
        meta.setCriteria(criteriaList);

        return meta;
    }

    private List<GroupAction> getGroupActions(JournalType type) {

        List<GroupAction> resultActions = new ArrayList<>();

        for (JournalGroupAction groupAction : type.getGroupActions()) {

            GroupAction action = new GroupAction();
            action.setId(groupAction.getId());
            action.setTitle(groupAction.getTitle());
            action.setParams(groupAction.getOptions());
            action.setType(groupAction.getType());
            if (groupAction.getViewClass() != null) {
                action.setFormKey("alf_" + groupAction.getViewClass());
            }

            resultActions.add(action);
        }

        for (String attribute : type.getAttributes()) {

            List<JournalBatchEdit> batchEdits = type.getBatchEdit(attribute);

            for (JournalBatchEdit batchEdit : batchEdits) {

                GroupAction action = new GroupAction();
                action.setTitle(batchEdit.getTitle());

                Map<String, String> params = new HashMap<>(batchEdit.getOptions());
                params.put("attribute", attribute);
                action.setParams(params);
                action.setId("batch-edit");
                action.setType("selected");

                resultActions.add(action);
            }
        }

        return resultActions;
    }

    private String getCriterionValue(String value) {

        if (StringUtils.isBlank(value)) {
            return value;
        }

        String template = Utils.restoreFreemarkerVariables(value);
        if (Objects.equals(value, template) && !value.contains("<#")) {
            return value;
        }

        Map<String, Object> model = RepoUtils.buildDefaultModel(serviceRegistry);
        return templateService.processTemplateString("freemarker", template, model);
    }

    private JournalRepoData getJournalRepoData(String journalId) {

        JournalRepoData repoData = new JournalRepoData();

        NodeRef journalRef = FTSQuery.create()
                                     .type(JournalsModel.TYPE_JOURNAL).and()
                                     .exact(JournalsModel.PROP_JOURNAL_TYPE, journalId)
                                     .transactional()
                                     .queryOne(searchService)
                                     .orElse(null);

        repoData.setNodeRef(journalRef);
        if (journalRef != null) {
            repoData.setCriteria(nodeUtils.getAssocTargets(journalRef, JournalsModel.ASSOC_SEARCH_CRITERIA));
        }

        return repoData;
    }

    private Map<String, AttInfo> getAttributesInfo(String sourceId, List<String> attributes) {

        Map<String, String> attributesEdges = new HashMap<>();
        for (String attribute : attributes) {
            attributesEdges.put(attribute, ".edge(n:\"" + attribute + "\"){type,editorKey,javaClass}");
        }

        RecordRef recordRef = RecordRef.create(sourceId, "");
        RecordMeta attInfoMeta = recordsService.getAttributes(recordRef, attributesEdges);

        Map<String, AttInfo> result = new HashMap<>();

        for (String attribute : attributes) {

            AttInfo info = null;

            JsonNode attInfoNode = attInfoMeta.get(attribute);
            if (attInfoNode instanceof ObjectNode) {
                try {
                    info = objectMapper.treeToValue(attInfoNode, AttInfo.class);
                } catch (JsonProcessingException e) {
                    logger.warn("Error", e);
                }
            }

            result.put(attribute, info != null ? info : new AttInfo());
        }

        return result;
    }

    private String toPrefix(QName name) {
        return name.toPrefixString(namespaceService);
    }

    @Autowired
    public void setCreateVariantsGet(CreateVariantsGet createVariantsGet) {
        this.createVariantsGet = createVariantsGet;
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.messageService = serviceRegistry.getMessageService();
        this.templateService = serviceRegistry.getTemplateService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    static class JournalRepoData {
        @Getter @Setter NodeRef nodeRef;
        @Getter @Setter List<NodeRef> criteria;
    }

    static class Response {
        @Getter @Setter String id;
        @Getter @Setter String sourceId;
        @Getter @Setter JournalMeta meta;
        @Getter @Setter List<Column> columns;
        @Getter @Setter Map<String, String> params;
    }

    static class JournalMeta {
        @Getter @Setter String nodeRef;
        @Getter @Setter List<Criterion> criteria;
        @Getter @Setter String title;
        @Getter @Setter JsonNode predicate;
        @Getter @Setter JsonNode groupBy;
        @Getter @Setter List<CreateVariantsGet.ResponseVariant> createVariants;
        @Getter @Setter List<GroupAction> groupActions;
    }

    static class GroupAction {
        @Getter @Setter String id;
        @Getter @Setter String title;
        @Getter @Setter Map<String, String> params;
        @Getter @Setter String type;
        @Getter @Setter String formKey;
    }

    static class Criterion {
        @Getter @Setter String field;
        @Getter @Setter String predicate;
        @Getter @Setter String value;
    }

    static class Column {
        @Getter @Setter String text;
        @Getter @Setter String type;
        @Getter @Setter String editorKey;
        @Getter @Setter String javaClass;
        @Getter @Setter String attribute;
        @Getter @Setter String schema;
        @Getter @Setter Formatter formatter;
        @Getter @Setter Map<String, String> params;
        @Getter @Setter boolean isDefault;
        @Getter @Setter boolean isSearchable;
        @Getter @Setter boolean isSortable;
        @Getter @Setter boolean isVisible;
        @Getter @Setter boolean isGroupable;
    }

    static class Formatter {
        @Getter @Setter String name;
        @Getter @Setter Map<String, String> params;
    }

    static class AttInfo {
        @Getter @Setter String type;
        @Getter @Setter String editorKey;
        @Getter @Setter Class<?> javaClass;
    }
}
