package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GraphQLMetaService;
import ru.citeck.ecos.graphql.meta.MetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaMapValue;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.history.HistoryEventType;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;
import ru.citeck.ecos.records.request.query.SortBy;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;
import ru.citeck.ecos.records.source.alfnode.meta.AlfNodeAttValue;
import ru.citeck.ecos.search.*;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.AuthorityUtils;
import ru.citeck.ecos.utils.search.SearchResult;
import ru.citeck.ecos.utils.search.SearchUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Task statistic journal prototype
 *
 * @author Pavel Simonov
 */
@Component
public class TaskStatisticRecords extends AbstractRecordsDAO implements RecordsWithMetaDAO {

    public static final String ID = "task-statistic";

    private static final String PERFORM_TIME_RATIO = "history:performTimeRatio";
    private static final String ACTUAL_PERFORM_TIME = "history:actualPerformTime";
    private static final String COMPLETION_DATE = "history:completionDate";
    private static final String EXPECTED_PERFORM_TIME = "history:expectedPerformTime";
    private static final String STARTED_DATE = "event:date";

    private static final String PRED_STR_EQUALS = SearchPredicate.STRING_EQUALS.getValue();

    private static final Log logger = LogFactory.getLog(TaskStatisticRecords.class);

    private SearchService searchService;
    private NamespaceService namespaceService;
    private SearchCriteriaParser criteriaParser;
    private AuthorityUtils authorityUtils;
    private FTSQueryBuilder queryBuilder;
    private SearchUtils searchUtils;
    private GraphQLMetaService metaService;
    private GraphQLService graphQLService;

    @Autowired
    public TaskStatisticRecords(ServiceRegistry serviceRegistry,
                                SearchCriteriaParser criteriaParser,
                                AuthorityUtils authorityUtils,
                                FTSQueryBuilder queryBuilder,
                                SearchUtils searchUtils,
                                GraphQLMetaService metaService,
                                GraphQLService graphQLService) {
        setId(ID);
        this.searchService = serviceRegistry.getSearchService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.graphQLService = graphQLService;
        this.authorityUtils = authorityUtils;
        this.criteriaParser = criteriaParser;
        this.queryBuilder = queryBuilder;
        this.searchUtils = searchUtils;
        this.metaService = metaService;
    }

    @Override
    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema) {

        RecordsResult<ObjectNode> records = new RecordsResult<>();
        RecordsResult<MetaValue> metaValues = getRecordsImpl(query);

        records.setRecords(metaService.getMeta(metaValues.getRecords(), metaSchema));
        records.setHasMore(metaValues.getHasMore());
        records.setTotalCount(metaValues.getTotalCount());

        return records;
    }

    private RecordsResult<MetaValue> getRecordsImpl(RecordsQuery query) {

        GqlContext context = graphQLService.getGqlContext();

        SearchResult<NodeRef> assignSearchResult = getAssignEvents(query);
        List<GqlAlfNode> assignNodes = wrapNodes(assignSearchResult.getItems(), context);
        assignNodes = mergeAssignEvents(assignNodes);

        List<MetaValue> records = new ArrayList<>();

        assignNodes.forEach(assignNode -> {

            if (records.size() == query.getMaxItems()) {
                return;
            }

            String taskId = (String) assignNode.getProperties().get(HistoryModel.PROP_TASK_INSTANCE_ID);

            List<GqlAlfNode> taskStartStopEvents = FTSQuery.create()
                    .value(HistoryModel.PROP_TASK_INSTANCE_ID, taskId).and()
                    .open()
                        .value(HistoryModel.PROP_NAME, HistoryEventType.TASK_CREATE).or()
                        .value(HistoryModel.PROP_NAME, HistoryEventType.TASK_COMPLETE)
                    .close()
                    .query(searchService)
                    .stream()
                    .map(context::getNode)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            Optional<GqlAlfNode> startEvent = Optional.empty();
            Optional<GqlAlfNode> completeEvent = Optional.empty();

            for (GqlAlfNode node : taskStartStopEvents) {
                String name = (String) node.getProperties().get(HistoryModel.PROP_NAME);
                if (HistoryEventType.TASK_CREATE.equals(name)) {
                    startEvent = Optional.of(node);
                } else if (HistoryEventType.TASK_COMPLETE.equals(name)) {
                    completeEvent = Optional.of(node);
                }
                if (startEvent.isPresent() && completeEvent.isPresent()) {
                    break;
                }
            }

            Map<String, Object> recordAttributes = getRecord(startEvent.orElse(null),
                                                             completeEvent.orElse(null),
                                                             assignNode,
                                                             context);

            if (recordAttributes != null) {
                MetaMapValue record = new MetaMapValue(taskId);
                record.setAttributes(recordAttributes);
                records.add(record);
            }
        });

        RecordsResult<MetaValue> result = new RecordsResult<>();
        result.setHasMore(assignSearchResult.getHasMore());
        result.setTotalCount(assignSearchResult.getTotalCount());
        result.setRecords(records);

        return result;
    }

    private List<GqlAlfNode> mergeAssignEvents(List<GqlAlfNode> events) {

        List<GqlAlfNode> result = new ArrayList<>();
        Map<String, Integer> eventsIndex = new HashMap<>();

        for (GqlAlfNode assignEvent : events) {

            String taskId = (String) assignEvent.getProperties().get(HistoryModel.PROP_TASK_INSTANCE_ID);

            if (StringUtils.isNotBlank(taskId)) {

                Integer idx = eventsIndex.computeIfAbsent(taskId, id -> {
                    result.add(assignEvent);
                    return result.size() - 1;
                });

                GqlAlfNode existing = result.get(idx);
                if (existing != assignEvent) {
                    Date assignDate = (Date) assignEvent.getProperties().get(HistoryModel.PROP_DATE);
                    Date existingDate = (Date) existing.getProperties().get(HistoryModel.PROP_DATE);
                    if (existingDate == null || assignDate != null && assignDate.after(existingDate)) {
                        result.set(idx, assignEvent);
                    }
                }
            }
        }

        return result;
    }

    private List<GqlAlfNode> wrapNodes(List<NodeRef> nodeRefs, GqlContext context) {
        return nodeRefs.stream()
                       .map(context::getNode)
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .collect(Collectors.toList());
    }

    private SearchResult<NodeRef> getAssignEvents(RecordsQuery query) {

        SearchCriteria criteria = criteriaParser.parse(query.getQuery());
        SearchCriteria processedCriteria = new SearchCriteria(namespaceService);

        String eventTypeProp = HistoryModel.PROP_NAME.toPrefixString(namespaceService);

        processedCriteria.addCriteriaTriplet(eventTypeProp, PRED_STR_EQUALS, HistoryEventType.TASK_ASSIGN);

        for (CriteriaTriplet triplet : criteria.getTriplets()) {

            String field = triplet.getField();
            String predicate = triplet.getPredicate();
            String value = triplet.getValue();

            QName fieldQName;
            try {
                fieldQName = QName.resolveToQName(namespaceService, triplet.getField());
            } catch (NamespaceException e) {
                logger.warn("Field " + triplet.getField() + " is not a valid QName. Ignore it");
                continue;
            }

            if (fieldQName.equals(HistoryModel.ASSOC_INITIATOR)) {
                if (value != null && NodeRef.isNodeRef(value)) {
                    NodeRef rootRef = new NodeRef(value);
                    Set<String> authorities = authorityUtils.getContainedUsers(rootRef, false);
                    if (!authorities.isEmpty()) {
                        value = authorityUtils.getNodeRefs(authorities)
                                              .stream()
                                              .map(Object::toString)
                                              .collect(Collectors.joining(","));
                    }
                }
            }

            processedCriteria.addCriteriaTriplet(field, predicate, value);
        }

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setMaxItems(query.getMaxItems() * 2);
        searchParameters.setSkipCount(query.getSkipCount());
        searchParameters.setQuery(queryBuilder.buildQuery(processedCriteria));

        String dateAttr = HistoryModel.PROP_DATE.toPrefixString(namespaceService);
        for (SortBy sortBy : query.getSortBy()) {
            if (sortBy.getAttribute().equals(dateAttr)) {
                searchParameters.addSort("@" + sortBy.getAttribute(), sortBy.isAscending());
            }
        }

        return searchUtils.query(searchParameters);
    }

    private Map<String, Object> getRecord(GqlAlfNode startEvent,
                                          GqlAlfNode endEvent,
                                          GqlAlfNode assignEvent,
                                          GqlContext context) {

        if (startEvent == null || assignEvent == null) {
            return null;
        }

        Map<String, Object> recordAttributes = new HashMap<>();

        String documentAttrName = HistoryModel.ASSOC_DOCUMENT.toPrefixString(namespaceService);
        MetaValue docAttributeGql = getAssocAttribute(startEvent, documentAttrName, context);
        recordAttributes.put(documentAttrName, docAttributeGql);

        Map<QName, Serializable> startedProps = startEvent.getProperties();
        startedProps.forEach((k, v) -> {
            String key = k.toPrefixString(namespaceService);
            if (v instanceof QName) {
                recordAttributes.put(key, context.getQName(v));
            } else {
                recordAttributes.put(key, v);
            }
        });

        Date startDate = (Date) startedProps.get(HistoryModel.PROP_DATE);
        recordAttributes.put(STARTED_DATE, ISO8601Utils.format(startDate));

        Date dueDate = (Date) startEvent.getProperties().get(HistoryModel.PROP_TASK_DUE_DATE);

        long expectedPerformTime = 0;
        if (dueDate != null) {
            long timeDiff = dueDate.getTime() - startDate.getTime();
            expectedPerformTime = Math.round(timeDiff / (1000f * 60 * 60));
            recordAttributes.put(EXPECTED_PERFORM_TIME, expectedPerformTime);
        }

        String initiatorAttrName = HistoryModel.ASSOC_INITIATOR.toPrefixString(namespaceService);
        recordAttributes.put(initiatorAttrName, getAssocAttribute(assignEvent, initiatorAttrName, context));

        if (endEvent != null) {
            Date completionDate = (Date) endEvent.getProperties().get(HistoryModel.PROP_DATE);
            recordAttributes.put(COMPLETION_DATE, ISO8601Utils.format(completionDate));
            long timeDiff = completionDate.getTime() - startDate.getTime();
            long actualPerformTime = Math.round(timeDiff / (1000f * 60 * 60));
            recordAttributes.put(ACTUAL_PERFORM_TIME, actualPerformTime);
            if (actualPerformTime > 0) {
                recordAttributes.put(PERFORM_TIME_RATIO, ((float) expectedPerformTime / actualPerformTime));
            } else {
                recordAttributes.put(PERFORM_TIME_RATIO, 1f);
            }
        }

        return recordAttributes;
    }

    private MetaValue getAssocAttribute(GqlAlfNode node, String key, GqlContext context) {
        return new AlfNodeAttValue(node.attribute(key)
                                       .node()
                                       .map(n -> new NodeRef(n.nodeRef()))
                                       .orElse(null)).init(context);
    }
}
