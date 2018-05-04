package ru.citeck.ecos.graphql;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.alfresco.repo.content.MimetypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.Map;

public class GraphQLWebscript extends AbstractWebScript {

    @Autowired
    private AlfGraphQLServiceImpl graphQLService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public GraphQLWebscript() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Request request = parseJSON(req);
        ExecutionResult result = graphQLService.execute(request.query, request.variables);

        objectMapper.writeValue(res.getOutputStream(), result.toSpecification());
        res.setStatus(Status.STATUS_OK);
    }

    private Request parseJSON(WebScriptRequest req) {

        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }

        if (MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
            try {
                String content = req.getContent().getContent();
                return objectMapper.readValue(content, Request.class);
            } catch (IOException e) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage(), e);
            }
        } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Content type must be JSON");
        }
    }

    private static class Request {
        public String query;
        public Map<String, Object> variables;
    }
}
