package ru.citeck.ecos.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.alfresco.repo.content.MimetypeMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class GraphQLWebscript extends AbstractWebScript {

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_PARAMETERS = "params";

    @Autowired
    GraphQLService graphQLService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        JSONObject json = parseJSON(req);

        String query = (String) json.get(PARAM_QUERY);
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) json.get(PARAM_PARAMETERS);
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }

        ExecutionResult result = graphQLService.execute(query, parameters);

        objectMapper.writeValue(res.getOutputStream(), result.toSpecification());
        res.setStatus(Status.STATUS_OK);
    }

    private JSONObject parseJSON(WebScriptRequest req) {

        JSONObject json;

        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }

        if (MimetypeMap.MIMETYPE_JSON.equals(contentType)) {

            JSONParser parser = new JSONParser();
            try {
                json = (JSONObject) parser.parse(req.getContent().getContent());
            } catch (IOException | ParseException io) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            }
        } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Content type must be JSON");
        }
        return json;
    }
}
