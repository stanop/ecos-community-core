package ru.citeck.ecos.journals.datasource;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.AlfNodeAttribute;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.record.attribute.JournalAttributeMapValue;
import ru.citeck.ecos.graphql.node.Attribute;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.history.HistoryEventType;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.search.CriteriaTriplet;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.search.ftsquery.QueryResult;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Task statistic journal prototype
 *
 * @author Pavel Simonov
 */
public class TaskStatisticDatasource implements JournalDataSource {

    private static final Log logger = LogFactory.getLog(TaskStatisticDatasource.class);

    private SearchService searchService;
    private NamespaceService namespaceService;
    private SearchCriteriaParser criteriaParser;
    private AssociationIndexPropertyRegistry assocsPropsRegistry;

    @Autowired
    public TaskStatisticDatasource(ServiceRegistry serviceRegistry,
                                   SearchCriteriaParser criteriaParser,
                                   AssociationIndexPropertyRegistry assocsPropsRegistry) {
        this.criteriaParser = criteriaParser;
        this.searchService = serviceRegistry.getSearchService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.assocsPropsRegistry = assocsPropsRegistry;
    }

    @Override
    public JournalRecordsConnection getRecords(GqlContext context,
                                               String query,
                                               String language,
                                               JournalGqlPageInfoInput pageInfo) {

        int maxItems = pageInfo.getMaxItems() * 2;

        FTSQuery searchQuery = FTSQuery.createRaw()
                                       .value(HistoryModel.PROP_NAME, HistoryEventType.TASK_ASSIGN)
                                       .maxItems(maxItems)
                                       .skipCount(pageInfo.getSkipCount());

        SearchCriteria criteria = criteriaParser.parse(query);

        for (CriteriaTriplet triplet : criteria.getTriplets()) {
            try {
                QName field = QName.resolveToQName(namespaceService, triplet.getField());
                if (field.equals(HistoryModel.ASSOC_INITIATOR)) {
                    QName assocPropName = assocsPropsRegistry.getAssociationIndexProperty(HistoryModel.ASSOC_INITIATOR);
                    searchQuery.and().value(assocPropName, triplet.getValue());
                    break;
                }
            } catch (NamespaceException e) {
                logger.warn("Field " + triplet.getField() + " is not a valid QName. Ignore it");
            }
        }

        QueryResult queryResult = searchQuery.queryDetails(searchService);
        List<GqlAlfNode> assignEvents = queryResult.getNodeRefs()
                                                   .stream()
                                                   .map(context::getNode)
                                                   .filter(Optional::isPresent)
                                                   .map(Optional::get)
                                                   .collect(Collectors.toList());

        Map<String, GqlAlfNode> assignNodes = new HashMap<>();
        for (GqlAlfNode assignNode : assignEvents) {
            String taskId = (String) assignNode.getProperties().get(HistoryModel.PROP_TASK_INSTANCE_ID);
            if (taskId != null) {
                GqlAlfNode storedEvent = assignNodes.get(taskId);
                if (storedEvent == null) {
                    assignNodes.put(taskId, assignNode);
                } else {
                    Date assignDate = (Date) assignNode.getProperties().get(HistoryModel.PROP_DATE);
                    Date storedAssignDate = (Date) storedEvent.getProperties().get(HistoryModel.PROP_DATE);
                    if (storedAssignDate == null || assignDate != null && assignDate.after(storedAssignDate)) {
                        assignNodes.put(taskId, assignNode);
                    }
                }
            }
        }

        List<JournalAttributeValueGql> records = new ArrayList<>();

        assignNodes.forEach((taskId, assignNode) -> {

            if (records.size() == pageInfo.getMaxItems()) {
                return;
            }

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
                JournalAttributeMapValue record = new JournalAttributeMapValue(taskId);
                record.setAttributes(recordAttributes);
                records.add(record);
            }
        });

        JournalRecordsConnection connection = new JournalRecordsConnection();
        connection.setRecords(records);

        int numberFound = queryResult.getNodeRefs().size();

        JournalGqlPageInfo outPageInfo = new JournalGqlPageInfo();
        outPageInfo.setHasNextPage(numberFound == maxItems || queryResult.hasMore());
        outPageInfo.setMaxItems(pageInfo.getMaxItems());
        outPageInfo.setSkipCount(pageInfo.getSkipCount());
        connection.setPageInfo(outPageInfo);

        connection.setTotalCount(queryResult.getTotalCount());

        return connection;

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
        JournalAttributeGql docAttributeGql = getAssocAttribute(startEvent, documentAttrName, context);
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
        recordAttributes.put("history:startedDate", ISO8601Utils.format(startDate));

        Date dueDate = (Date) startEvent.getProperties().get(HistoryModel.PROP_TASK_DUE_DATE);

        long expectedPerformTime = 0;
        if (dueDate != null) {
            long timeDiff = dueDate.getTime() - startDate.getTime();
            expectedPerformTime = Math.round(timeDiff / (1000f * 60 * 60));
            recordAttributes.put("history:expectedPerformTime", expectedPerformTime);
        }

        String initiatorAttrName = HistoryModel.ASSOC_INITIATOR.toPrefixString(namespaceService);
        recordAttributes.put(initiatorAttrName, getAssocAttribute(assignEvent, initiatorAttrName, context));

        if (endEvent != null) {
            Date completionDate = (Date) endEvent.getProperties().get(HistoryModel.PROP_DATE);
            recordAttributes.put("history:completionDate", ISO8601Utils.format(completionDate));
            long timeDiff = completionDate.getTime() - startDate.getTime();
            long actualPerformTime = Math.round(timeDiff / (1000f * 60 * 60));
            recordAttributes.put("history:actualPerformTime", actualPerformTime);
            if (actualPerformTime > 0) {
                recordAttributes.put("history:performTimeRatio", ((float) expectedPerformTime / actualPerformTime));
            } else {
                recordAttributes.put("history:performTimeRatio", 1f);
            }
        }

        return recordAttributes;
    }

    private AlfNodeAttribute getAssocAttribute(GqlAlfNode node, String key, GqlContext context) {
        Attribute initiatorAttr = node.attribute(key);
        return new AlfNodeAttribute(initiatorAttr, context);
    }

    @Override
    public Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }
}
