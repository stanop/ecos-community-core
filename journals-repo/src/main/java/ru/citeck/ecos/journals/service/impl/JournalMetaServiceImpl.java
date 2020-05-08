package ru.citeck.ecos.journals.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ecos.com.fasterxml.jackson210.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.journals.JournalBatchEdit;
import ru.citeck.ecos.journals.JournalGroupAction;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalMeta;
import ru.citeck.ecos.journals.domain.JournalRepoData;
import ru.citeck.ecos.journals.service.JournalMetaService;
import ru.citeck.ecos.journals.webscripts.CreateVariantsGet;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.records.source.alf.AlfDictionaryRecords;
import ru.citeck.ecos.records.source.alf.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.records2.predicate.PredicateService;
import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.records2.predicate.model.ValuePredicate;
import ru.citeck.ecos.records2.querylang.QueryLangService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.server.utils.Utils;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JournalMetaServiceImpl implements JournalMetaService {

    private static final String META_RECORD_TEMPLATE = AlfDictionaryRecords.ID + "@%s";

    private LoadingCache<String, NodeRef> journalRefById;
    private LoadingCache<NodeRef, JournalRepoData> repoDataByJournalRef;

    private JournalService journalService;
    private NodeUtils nodeUtils;
    private NodeService nodeService;
    private SearchService searchService;
    private QueryLangService queryLangService;
    private ServiceRegistry serviceRegistry;
    private TemplateService templateService;
    private NamespaceService namespaceService;
    //  wrong to use controllers or webscripts in business-layer.
    //  TODO: extract interface that required here and use it.
    private CreateVariantsGet createVariantsGet;

    @Autowired
    public JournalMetaServiceImpl(ServiceRegistry serviceRegistry,
                                  QueryLangService queryLangService,
                                  NodeUtils nodeUtils,
                                  JournalService journalService,
                                  CreateVariantsGet createVariantsGet) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.queryLangService = queryLangService;
        this.nodeUtils = nodeUtils;
        this.serviceRegistry = serviceRegistry;
        this.templateService = serviceRegistry.getTemplateService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.createVariantsGet = createVariantsGet;
        this.journalService = journalService;

        journalRefById = CacheBuilder.newBuilder()
            .expireAfterWrite(600, TimeUnit.SECONDS)
            .maximumSize(200)
            .build(CacheLoader.from(this::findJournalRef));
        repoDataByJournalRef = CacheBuilder.newBuilder()
            .expireAfterWrite(600, TimeUnit.SECONDS)
            .maximumSize(200)
            .build(CacheLoader.from(this::getJournalRepoData));
    }

    private NodeRef findJournalRef(String journalId) {
        return FTSQuery.create()
            .type(JournalsModel.TYPE_JOURNAL).and()
            .exact(JournalsModel.PROP_JOURNAL_TYPE, journalId)
            .transactional()
            .queryOne(searchService)
            .orElse(null);
    }

    private JournalRepoData getJournalRepoData(NodeRef nodeRef) {

        JournalRepoData repoData = new JournalRepoData();

        repoData.setNodeRef(nodeRef);
        if (nodeRef != null) {
            repoData.setCriteria(nodeUtils.getAssocTargets(nodeRef, JournalsModel.ASSOC_SEARCH_CRITERIA));
        }

        return repoData;
    }

    @Override
    public JournalMeta getJournalMeta(String journalId) {

        NodeRef journalNodeRef = NodeRef.isNodeRef(journalId) ? new NodeRef(journalId) : null;
        if (journalNodeRef == null) {
            journalNodeRef = journalRefById.getUnchecked(journalId);
        }
        return this.getJournalMeta(journalNodeRef);
    }

    @Override
    public JournalMeta getJournalMeta(NodeRef journalNodeRef) {
        JournalType journalType = journalService.getJournalType(journalNodeRef);

        JournalRepoData journalData = repoDataByJournalRef.getUnchecked(journalNodeRef);
        JournalMeta meta = new JournalMeta();

        meta.setActions(journalType.getActions());
        meta.setGroupActions(this.getGroupActions(journalType));

        if (StringUtils.isNotBlank(journalType.getGroupBy())) {
            meta.setGroupBy(Json.getMapper().read(journalType.getGroupBy()));
        }

        if (StringUtils.isNotBlank(journalType.getPredicate())) {
            meta.setPredicate(Json.getMapper().read(journalType.getPredicate()));
        }
        meta.setCreateVariants(createVariantsGet.getVariantsByJournalRef(journalData.getNodeRef(), true));

        fillMetaFromRepo(meta, journalData);

        Map<String, String> options = journalType.getOptions();

        String type = MapUtils.getString(options, "type");
        if (meta.getPredicate() == null && StringUtils.isNotBlank(type)) {
            Predicate predicate = ValuePredicate.equal("TYPE", type);
            meta.setPredicate(Json.getMapper().convert(predicate, JsonNode.class));
        }

        if (options == null) {
            options = Collections.emptyMap();
        }

        String metaRecord = options.get("metaRecord");

        if (StringUtils.isNotBlank(metaRecord)) {

            meta.setMetaRecord(metaRecord);

        } else if (StringUtils.isNotBlank(journalType.getDataSource())) {

            meta.setMetaRecord(journalType.getDataSource() + "@");

        } else if (StringUtils.isNotBlank(type)) {

            meta.setMetaRecord(String.format(META_RECORD_TEMPLATE, type));
        }

        return meta;
    }

    private void fillMetaFromRepo(JournalMeta meta, JournalRepoData journalData) {

        if (journalData.getNodeRef() == null) {
            return;
        }

        NodeRef journalRef = journalData.getNodeRef();
        meta.setNodeRef(String.valueOf(journalRef));
        meta.setTitle(nodeUtils.getDisplayName(journalRef));

        List<JournalMeta.Criterion> criteriaList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria(namespaceService);

        for (NodeRef criterionRef : journalData.getCriteria()) {

            Map<QName, Serializable> props = nodeService.getProperties(criterionRef);

            JournalMeta.Criterion criterion = new JournalMeta.Criterion();
            criterion.setField(toPrefix((QName) props.get(JournalsModel.PROP_FIELD_QNAME)));
            criterion.setPredicate((String) props.get(JournalsModel.PROP_PREDICATE));
            criterion.setValue(getCriterionValue((String) props.get(JournalsModel.PROP_CRITERION_VALUE)));

            criteria.addCriteriaTriplet(criterion.getField(), criterion.getPredicate(), criterion.getValue());

            criteriaList.add(criterion);
        }

        meta.setCriteria(criteriaList);

        if (meta.getPredicate() == null) {

            JsonNode criteriaJson = Json.getMapper().toJson(criteria);
            try {
                Object convertedQuery = queryLangService.convertLang(criteriaJson,
                    CriteriaAlfNodesSearch.LANGUAGE,
                    PredicateService.LANGUAGE_PREDICATE)
                    .orElseThrow(RuntimeException::new);
                meta.setPredicate(Json.getMapper().convert(convertedQuery, JsonNode.class));
            } catch (Exception e) {
                log.error("Language conversion error. criteria: " + criteriaJson, e);
            }
        }
    }

    private List<JournalMeta.GroupAction> getGroupActions(JournalType type) {

        List<JournalMeta.GroupAction> resultActions = new ArrayList<>();

        for (JournalGroupAction groupAction : type.getGroupActions()) {

            JournalMeta.GroupAction action = new JournalMeta.GroupAction();
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

                JournalMeta.GroupAction action = new JournalMeta.GroupAction();
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

    private String toPrefix(QName name) {
        return name.toPrefixString(namespaceService);
    }
}
