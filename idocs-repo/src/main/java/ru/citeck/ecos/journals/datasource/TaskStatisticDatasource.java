package ru.citeck.ecos.journals.datasource;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.search.CriteriaTriplet;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.search.ftsquery.QueryResult;
import ru.citeck.ecos.utils.NodeUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Task statistic journal prototype
 *
 * @author Pavel Simonov
 */
public class TaskStatisticDatasource implements JournalDataSource {

    private static final Log logger = LogFactory.getLog(TaskStatisticDatasource.class);

    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private SearchCriteriaParser criteriaParser;
    private WorkflowService workflowService;
    private AssociationIndexPropertyRegistry assocsPropsRegistry;

    private NodeUtils nodeUtils;

    @Autowired
    public TaskStatisticDatasource(ServiceRegistry serviceRegistry,
                                   SearchCriteriaParser criteriaParser,
                                   AssociationIndexPropertyRegistry assocsPropsRegistry,
                                   NodeUtils nodeUtils) {
        this.criteriaParser = criteriaParser;
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.assocsPropsRegistry = assocsPropsRegistry;
        this.nodeUtils = nodeUtils;
    }

    @Override
    public JournalRecordsConnection getRecords(GqlContext context,
                                               String query,
                                               String language,
                                               JournalGqlPageInfoInput pageInfo) {
        boolean searchByStartedEvent = true;

        FTSQuery searchQuery = FTSQuery.createRaw()
                                       .maxItems(pageInfo.getMaxItems())
                                       .skipCount(pageInfo.getSkipCount());

        SearchCriteria criteria = criteriaParser.parse(query);

        for (CriteriaTriplet triplet : criteria.getTriplets()) {
            try {
                QName field = QName.resolveToQName(namespaceService, triplet.getField());
                if (field.equals(HistoryModel.ASSOC_INITIATOR)) {
                    searchByStartedEvent = false;
                    QName assocPropName = assocsPropsRegistry.getAssociationIndexProperty(HistoryModel.ASSOC_INITIATOR);
                    searchQuery.value(HistoryModel.PROP_NAME, "task.complete").and()
                               .value(assocPropName, triplet.getValue());
                    break;
                }
            } catch (NamespaceException e) {
                logger.warn("Field " + triplet.getField() + " is not a valid QName. Ignore it");
            }
        }

        if (searchByStartedEvent) {
            searchQuery.value(HistoryModel.PROP_NAME, "task.create");
        }

        QueryResult queryResult = searchQuery.queryDetails(searchService);
        List<NodeRef> eventsNodes = queryResult.getNodeRefs();

        List<JournalAttributeValueGql> records = new ArrayList<>();

        for (NodeRef firstEventRef : eventsNodes) {

            Optional<GqlAlfNode> optFirstEventNode = context.getNode(firstEventRef);
            if (!optFirstEventNode.isPresent()) {
                continue;
            }

            String taskId = (String) optFirstEventNode.get().getProperties().get(HistoryModel.PROP_TASK_INSTANCE_ID);
            Optional<NodeRef> optSecondEvent = FTSQuery.create()
                    .value(HistoryModel.PROP_TASK_INSTANCE_ID, taskId).and()
                    .value(HistoryModel.PROP_NAME, searchByStartedEvent ? "task.complete" : "task.create")
                    .queryOne(searchService);

            Optional<GqlAlfNode> optSecondEventNode = optSecondEvent.flatMap(context::getNode);


            Map<String, Object> recordAttributes;

            if (searchByStartedEvent) {
                recordAttributes = getRecord(optFirstEventNode.orElse(null),
                                             optSecondEventNode.orElse(null),
                                             context);
            } else {
                recordAttributes = getRecord(optSecondEventNode.orElse(null),
                                             optFirstEventNode.orElse(null),
                                             context);
            }

            if (recordAttributes != null) {
                JournalAttributeMapValue record = new JournalAttributeMapValue(firstEventRef.toString());
                record.setAttributes(recordAttributes);
                records.add(record);
            }
        }

        JournalRecordsConnection connection = new JournalRecordsConnection();
        connection.setRecords(records);

        JournalGqlPageInfo outPageInfo = new JournalGqlPageInfo();
        outPageInfo.setHasNextPage(queryResult.hasMore());
        outPageInfo.setMaxItems(pageInfo.getMaxItems());
        outPageInfo.setSkipCount(pageInfo.getSkipCount());
        connection.setPageInfo(outPageInfo);

        connection.setTotalCount(queryResult.getTotalCount());

        return connection;

    }

    private Map<String, Object> getRecord(GqlAlfNode startEvent, GqlAlfNode endEvent, GqlContext context) {

        if (startEvent == null) {
            return null;
        }

        String documentAttrName = HistoryModel.ASSOC_DOCUMENT.toPrefixString(namespaceService);
        Attribute documentAttr = startEvent.attribute(documentAttrName);
        JournalAttributeGql docAttributeGql = new AlfNodeAttribute(documentAttr, context);

        Map<String, Object> recordAttributes = new HashMap<>();

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

        NodeRef caseTask = (NodeRef) startedProps.get(HistoryModel.PROP_CASE_TASK);

        int expectedPerformTime = 0;
        if (caseTask != null) {
            Integer time = (Integer) nodeService.getProperty(caseTask, ActivityModel.PROP_EXPECTED_PERFORM_TIME);
            if (time != null) {
                expectedPerformTime = time;
                recordAttributes.put("history:expectedPerformTime", time);
            }
        }

        final int finalExpPerformTime = expectedPerformTime;

        if (endEvent != null) {

            Date completionDate = (Date) endEvent.getProperties().get(HistoryModel.PROP_DATE);
            recordAttributes.put("history:completionDate", ISO8601Utils.format(completionDate));
            long actualPerformTime = (completionDate.getTime() - startDate.getTime()) / (1000 * 60 * 60);
            recordAttributes.put("history:actualPerformTime", actualPerformTime);
            if (actualPerformTime > 0) {
                recordAttributes.put("history:performTimeRatio", ((float) finalExpPerformTime / actualPerformTime));
            } else {
                recordAttributes.put("history:performTimeRatio", 1f);
            }

            String initiatorAttrName = HistoryModel.ASSOC_INITIATOR.toPrefixString(namespaceService);

            Attribute initiatorAttr = endEvent.attribute(initiatorAttrName);
            AlfNodeAttribute alfNodeInitiatorAttribute = new AlfNodeAttribute(initiatorAttr, context);
            recordAttributes.put(initiatorAttrName, alfNodeInitiatorAttribute);
        }

        return recordAttributes;
    }

    @Override
    public Optional<JournalAttributeInfoGql> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }
}
