package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlRecordsInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverter;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverterFactory;
import ru.citeck.ecos.journals.records.GqlQueryExecutor;
import ru.citeck.ecos.records.RecordRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRecordsMetadataPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private GraphQLService graphQLService;
    private ServiceRegistry serviceRegistry;

    private ResponseConverterFactory responseConverterFactory = new ResponseConverterFactory();

    public GetRecordsMetadataPost() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Request request = parseRequest(webScriptRequest);

        ExecutionResult executionResult = executeQuery(request.gqlQuery, request.datasource, request.remoteRefs);

        JournalDataSource dataSource = findJournalDataSource(request.datasource);
        ResponseConverter converter = responseConverterFactory.getConverter(dataSource);
        JournalData journalData = converter.convert(executionResult, request.additionalData);

        webScriptResponse.setContentEncoding("UTF-8");
        webScriptResponse.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(webScriptResponse.getOutputStream(), journalData);
        webScriptResponse.setStatus(Status.STATUS_OK);
    }

    private JournalDataSource findJournalDataSource(String datasource) {
        QName datasourceQname = QName.createQName(null, datasource);
        JournalDataSource dataSource;
        try {
            dataSource = (JournalDataSource) serviceRegistry.getService(datasourceQname);
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Error while finding datasource: " + datasource + ". Error: " + e.getMessage(), e);
        }
        return dataSource;
    }

    private ExecutionResult executeQuery(String gqlQuery, String datasource, List<RecordRef> remoteRefs) {
        List<String> recordIds = new ArrayList<>(remoteRefs.size());
        remoteRefs.forEach(item -> recordIds.add(item.getId()));

        Map<String, Object> params = new HashMap<>();
        params.put(GqlQueryExecutor.GQL_PARAM_DATASOURCE, datasource);
        params.put(GqlQueryExecutor.GQL_PARAM_REMOTE_REFS, new JGqlRecordsInput(recordIds));

        return graphQLService.execute(gqlQuery, params);
    }

    private Request parseRequest(WebScriptRequest req) {
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }

        if (MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
            try {
                String content = req.getContent().getContent();
                return objectMapper.readValue(content, GetRecordsMetadataPost.Request.class);
            } catch (IOException e) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage(), e);
            }
        } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Content type must be JSON");
        }
    }

    private static class Request {
        public String datasource;
        public String gqlQuery;
        public List<RecordRef> remoteRefs;
        public Map<String, Object> additionalData;
    }

    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}
