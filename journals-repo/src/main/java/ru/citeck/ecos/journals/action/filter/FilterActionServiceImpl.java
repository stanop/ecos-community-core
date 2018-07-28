package ru.citeck.ecos.journals.action.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JournalGqlSortBy;
import ru.citeck.ecos.journals.records.JournalRecordsDAO;
import ru.citeck.ecos.journals.action.group.GroupActionResult;
import ru.citeck.ecos.journals.records.RecordsResult;
import ru.citeck.ecos.repo.RemoteNodeRef;
import ru.citeck.ecos.search.CriteriaTriplet;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;
import ru.citeck.ecos.search.SearchCriteriaSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterActionServiceImpl implements FilterActionService {

    private static final Log logger = LogFactory.getLog(FilterActionServiceImpl.class);

    private JournalRecordsDAO recordsDAO;
    private NodeService nodeService;
    private SearchCriteriaParser criteriaParser;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FilterActionServiceImpl(ServiceRegistry serviceRegistry,
                                   JournalRecordsDAO recordsDAO,
                                   SearchCriteriaParser criteriaParser) {

        this.recordsDAO = recordsDAO;
        this.nodeService = serviceRegistry.getNodeService();
        this.criteriaParser = criteriaParser;

        SimpleModule module = new SimpleModule();
        module.addSerializer(new SearchCriteriaSerializer());
        objectMapper.registerModule(module);
    }

    public Map<NodeRef, GroupActionResult> invoke(String searchCriteria,
                                                  String journalId,
                                                  String language,
                                                  String actionId,
                                                  Map<String, String> params) {

        SearchCriteria criteria = criteriaParser.parse(searchCriteria);
        return invoke(criteria, journalId, language, actionId, params);
    }

    @Override
    public Map<NodeRef, GroupActionResult> invoke(SearchCriteria searchCriteria,
                                                  String journalId,
                                                  String language,
                                                  String actionId,
                                                  Map<String, String> params) {

        SearchCriteria criteria = new SearchCriteria(searchCriteria);
        setSinceDBId(criteria, 0);

        List<JournalGqlSortBy> sortList = new ArrayList<>(1);
        sortList.add(new JournalGqlSortBy("sys:node-dbid", "asc"));

        JournalGqlPageInfoInput pageInfo = new JournalGqlPageInfoInput(0, 100, sortList);

        String query;
        try {
            query = objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't serialize criteria. " + criteria +
                    " actionId: " + actionId + " params " + params);
        }

        long currentDbId = 0;

        RecordsResult records  = recordsDAO.getRecords(query, language, journalId, pageInfo);

        while (records.records.size() > 0) {

            for (RemoteNodeRef record : records.records) {

                if (record.isLocal()) {

                    logger.warn("PROCESS: " + record);

                    Long dbid = (Long) nodeService.getProperty(record.getNodeRef(), ContentModel.PROP_NODE_DBID);
                    currentDbId = Math.max(currentDbId, dbid);

                } else {
                    logger.warn("Not local refs is not supported yet. skip: " + record);
                }
            }

            setSinceDBId(criteria, currentDbId);
            records  = recordsDAO.getRecords(query, language, journalId, pageInfo);
        }

        return null;
    }

    void setSinceDBId(SearchCriteria criteria, long id) {
        CriteriaTriplet dbIdTriplet = new CriteriaTriplet("sys:node-dbid",
                "number-greater-than",
                String.valueOf(id));
        criteria.replaceOrAddTriplet(dbIdTriplet);
    }

    @Override
    public void register(FilterActionExecutor executor) {

    }
}
