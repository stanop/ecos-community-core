package ru.citeck.ecos.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.GraphQLMetaServiceImpl;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.source.meta.MetaJsonNodeValue;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;
import ru.citeck.ecos.webscripts.history.DocumentHistoryGet;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HistoryRecordsDAO extends AbstractRecordsDAO implements RecordsWithMetaDAO {

    private static final String ID = "history";
    private static final String LANGUAGE_DOCUMENT = "document";

    private DocumentHistoryGet historyGet;
    private GraphQLMetaServiceImpl graphQLMetaService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public HistoryRecordsDAO() {
        setId(ID);
    }

    @Override
    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema) {

        String language = query.getLanguage();

        if (StringUtils.isBlank(language)) {
            language = LANGUAGE_DOCUMENT;
        }
        if (!LANGUAGE_DOCUMENT.equals(language)) {
            throw new IllegalArgumentException("Language " + language + " is not supported!");
        }

        Query queryData;
        try {
            queryData = objectMapper.readValue(query.getQuery(), Query.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<ObjectNode> events = historyGet.getHistoryEvents(queryData.nodeRef,
                                                              queryData.filter,
                                                              queryData.events,
                                                              queryData.taskTypes);

        List<ObjectNode> nodes = graphQLMetaService.getMeta(context -> getMetaValues(events, context), metaSchema);

        RecordsResult<ObjectNode> result = new RecordsResult<>();
        result.setHasMore(false);
        result.setTotalCount(nodes.size());
        result.setRecords(nodes);

        return result;
    }

    private List<MetaValue> getMetaValues(List<ObjectNode> events, GqlContext context) {
        return events.stream()
                     .map(e -> new MetaJsonNodeValue(e.get("nodeRef").asText(), e.get("attributes"), context))
                     .collect(Collectors.toList());
    }

    @Autowired
    public void setHistoryGet(DocumentHistoryGet historyGet) {
        this.historyGet = historyGet;
    }

    @Autowired
    public void setGraphQLMetaService(GraphQLMetaServiceImpl graphQLMetaService) {
        this.graphQLMetaService = graphQLMetaService;
    }

    public static class Query {
        public String nodeRef;
        public String filter;
        public String events;
        public String taskTypes;
    }
}
