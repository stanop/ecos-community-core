package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.AlfGraphQLServiceImpl;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlRecordsInput;
import ru.citeck.ecos.repo.RemoteRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRecordsMetadataPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private GraphQLService graphQLService;

    public GetRecordsMetadataPost() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Request request = parseRequest(webScriptRequest);
        ExecutionResult executionResult = executeQuery(request.gqlQuery, request.datasource, request.remoteRefs);
        objectMapper.writeValue(webScriptResponse.getOutputStream(), executionResult);
        webScriptResponse.setStatus(Status.STATUS_OK);
    }

    private ExecutionResult executeQuery(String gqlQuery, String datasource, List<RemoteRef> remoteRefs) {
        List<String> recordIds = new ArrayList<>(remoteRefs.size());
        remoteRefs.forEach(item -> {
            NodeRef itemRef = item.getNodeRef();
            recordIds.add(itemRef.toString());
        });

        Map<String, Object> params = new HashMap<>();
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_DATASOURCE, datasource);
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_REMOTE_REFS, new JGqlRecordsInput(recordIds));

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
        public List<RemoteRef> remoteRefs;
    }

    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }
}
