package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.predicate.model.Predicate;
import ru.citeck.ecos.records.source.alf.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.utils.RecordsUtils;
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

        if (NodeRef.isNodeRef(journalId)) {
            journalId = nodeUtils.getProperty(new NodeRef(journalId), JournalsModel.PROP_JOURNAL_TYPE);
        }

        JournalType type = journalService.getJournalType(journalId);

        if (type == null) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Journal with id '" + journalId + "' not found!");
        }

        List<String> attributes = type.getAttributes();
        List<Column> columns = new ArrayList<>();
        String sourceId = type.getDataSource();
        if (sourceId == null) {
            sourceId = "";
        }

        Map<String, Class<?>> columnClasses = RecordsUtils.getAttributesClasses(sourceId,
                                                                                attributes,
                                                                                null,
                                                                                recordsService);
        for (String name : attributes) {

            Column column = new Column();

            column.setDefault(type.isAttributeDefault(name));
            column.setGroupable(type.isAttributeGroupable(name));
            column.setSearchable(type.isAttributeSearchable(name));
            column.setSortable(type.isAttributeSortable(name));
            column.setVisible(type.isAttributeVisible(name));
            column.setAttribute(name);
            column.setParams(type.getAttributeOptions(name));
            column.setText(getColumnLabel(column));

            Class<?> javaClass = columnClasses.get(name);
            column.setJavaClass(javaClass != null ? javaClass.getName() : null);

            columns.add(column);
        }

        Response response = new Response();
        response.setId(journalId);
        response.setColumns(columns);
        response.setMeta(getJournalMeta(journalId));
        response.setSourceId(sourceId);
        response.setParams(type.getOptions());

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

        return column.getAttribute();
    }

    private JournalMeta getJournalMeta(String id) {

        JournalRepoData journalData = journalRefById.getUnchecked(id);

        JournalMeta meta = new JournalMeta();
        if (journalData.getNodeRef() == null) {
            return meta;
        }

        meta.setNodeRef(String.valueOf(journalData.getNodeRef()));

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

        JsonNode criteriaJson = objectMapper.valueToTree(criteria);
        try {
            meta.setPredicate(recordsService.convertQueryLanguage(criteriaJson,
                                                                  CriteriaAlfNodesSearch.LANGUAGE,
                                                                  RecordsService.LANGUAGE_PREDICATE));
        } catch (Exception e) {
            logger.error("Language conversion error. criteria: " + criteriaJson);
        }

        meta.setCreateVariants(createVariantsGet.getVariantsByJournalId(id, true));
        meta.setCriteria(criteriaList);

        return meta;
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
        @Getter @Setter JsonNode predicate;
        @Getter @Setter List<CreateVariantsGet.ResponseVariant> createVariants;
    }

    static class Criterion {
        @Getter @Setter String field;
        @Getter @Setter String predicate;
        @Getter @Setter String value;
    }

    static class Column {
        @Getter @Setter String text;
        @Getter @Setter String type;
        @Getter @Setter String javaClass;
        @Getter @Setter String attribute;
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
}
