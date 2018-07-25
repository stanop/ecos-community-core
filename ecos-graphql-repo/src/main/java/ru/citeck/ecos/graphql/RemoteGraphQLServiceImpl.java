package ru.citeck.ecos.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.language.SourceLocation;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.graphql.exceptions.CiteckGraphQLException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Remote graph-ql service implementation
 */
@Service("remoteGraphQLService")
public class RemoteGraphQLServiceImpl implements GraphQLService {

    /**
     * Logger
     */
    private static Log LOGGER = LogFactory.getLog(RemoteGraphQLServiceImpl.class);

    /**
     * Constants
     */
    private static final String DEFAULT_REMOTE_GRAPHQL_SERVICE_HOST = "http://localhost:8080/alfresco/service";
    private static final String REMOTE_GRAPHQL_METHOD = "/ecos/graphql";

    /**
     * Properties keys
     */
    private static final String REMOTE_GRAPHQL_SERVICE_HOST_PROP_KEY = "remote.graphql.service.host";
    private static final String REMOTE_GRAPHQL_SERVICE_USERNAME_PROP_KEY = "remote.graphql.service.username";
    private static final String REMOTE_GRAPHQL_SERVICE_PASSWORD_PROP_KEY = "remote.graphql.service.password";

    /**
     * Object mapper
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Rest template
     */
    private RestTemplate restTemplate;

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    /**
     * Init
     */
    @PostConstruct
    private void init() {
        String username = properties.getProperty(REMOTE_GRAPHQL_SERVICE_USERNAME_PROP_KEY);
        String password = properties.getProperty(REMOTE_GRAPHQL_SERVICE_PASSWORD_PROP_KEY);
        if (username != null && password != null) {
            this.restTemplate = new RestTemplate(this.createSecureTransport(username, password));
        } else {
            this.restTemplate = new RestTemplate();
        }
        /** Message converters */
        StringHttpMessageConverter utfMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        restTemplate.getMessageConverters().add(0, utfMessageConverter);
    }

    /**
     * Create secure transport
     * @param username Username
     * @param password Password
     * @return Secure transport
     */
    protected ClientHttpRequestFactory createSecureTransport(String username, String password){
        HttpClient client = new HttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, credentials);
        return new CommonsClientHttpRequestFactory(client);
    }

    /**
     * Execute
     * @param query Query
     * @return Execution result
     */
    @Override
    public ExecutionResult execute(String query) {
        String rawResult = postForRemoteService(query, Collections.emptyMap());
        return parseRawResult(rawResult);
    }

    /**
     * Execute
     * @param query Query
     * @param variables Variables
     * @return Execution result
     */
    @Override
    public ExecutionResult execute(String query, Map<String, Object> variables) {
        String rawResult = postForRemoteService(query, variables);
        return parseRawResult(rawResult);
    }

    /**
     * Parse raw result
     * @param rawResult Raw json result
     * @return Execution result
     */
    private ExecutionResult parseRawResult(String rawResult) {
        try {
            ObjectNode resultNode = objectMapper.readValue(rawResult, ObjectNode.class);
            /** Data */
            JsonNode dataNode = resultNode.get("data");
            Map<String, Object> data = objectMapper.readValue(dataNode.toString(), Map.class);
            /** Errors */
            ArrayList<CiteckGraphQLException> errorsList = new ArrayList<>();
            ArrayNode errors = (ArrayNode) resultNode.get("errors");
            for (JsonNode node : errors) {
                Map<String, Object> errorMap = objectMapper.readValue(node.toString(), Map.class);
                errorsList.add(parseError(errorMap));
            }
            return new ExecutionResultImpl(data, errorsList);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse error
     * @param rawMap Raw error map
     * @return GraphQL Error
     */
    private CiteckGraphQLException parseError(Map<String, Object> rawMap) {
        CiteckGraphQLException error = new CiteckGraphQLException();
        /** Message */
        if (rawMap.containsKey("message")) {
            error.setMessage((String) rawMap.get("message"));
        }
        /** Locations */
        if (rawMap.containsKey("locations")) {
            List<Map> rawLocations = (List<Map>) rawMap.get("locations");
            List<SourceLocation> locations = new ArrayList<>(rawLocations.size());
            for (Map rawLocation : rawLocations) {
                locations.add(new SourceLocation(
                        (Integer) rawLocation.get("line"),
                        (Integer) rawLocation.get("column")
                ));
            }
            error.setLocations(locations);
        }
        return error;
    }

    /**
     * Post query for remote service
     * @param query Query
     * @param variables Variables
     * @return Raw string result
     */
    private String postForRemoteService(String query, Map<String, Object> variables) {
        /** Header */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        /** Call remote service */
        HttpEntity requestEntity = new HttpEntity<>(createRequestBody(query, variables), headers);
        return restTemplate.postForObject(getRemoteGraphQlServiceHost() + REMOTE_GRAPHQL_METHOD, requestEntity, String.class);
    }

    /**
     * Create request body
     * @param query Query
     * @param variables Variables
     * @return Body string (json)
     */
    private String createRequestBody(String query, Map<String, Object> variables) {
        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("query", query);
        bodyNode.put("operationName", "SomeQUery");
        /** Variables */
        if (variables != null) {
            JSONObject variablesRawNode = new JSONObject(variables);
            ObjectNode variableNode = null;
            try {
                variableNode = objectMapper.readValue(variablesRawNode.toString(), ObjectNode.class);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                e.printStackTrace();
            }
            if (variableNode != null) {
                bodyNode.put("variables", variableNode);
            } else {
                bodyNode.putNull("variables");
            }
        } else {
            bodyNode.putNull("variables");
        }
        return bodyNode.toString();
    }

    /**
     * Get remote graph-ql service host
     * @return Service host (default value - http://localhost:8080)
     */
    private String getRemoteGraphQlServiceHost() {
        String host = properties.getProperty(REMOTE_GRAPHQL_SERVICE_HOST_PROP_KEY);
        return host != null ? host : DEFAULT_REMOTE_GRAPHQL_SERVICE_HOST;
    }
}
