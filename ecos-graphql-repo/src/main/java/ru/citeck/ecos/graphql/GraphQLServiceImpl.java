package ru.citeck.ecos.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.*;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.language.ObjectTypeDefinition;
import graphql.language.SourceLocation;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.graphql.exceptions.CiteckGraphQLException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GraphQLServiceImpl implements GraphQLService {

    private static final Log logger = LogFactory.getLog(GraphQLServiceImpl.class);

    private static final String GRAPHQL_BASE_PACKAGE = "ru.citeck.ecos";
    private static final String QUERY_TYPE = "Query";

    @Autowired
    private ServiceRegistry serviceRegistry;

    private GraphQL graphQL;

    @PostConstruct
    void init() {

        ClassPathScanningCandidateComponentProvider scanner;
        scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(GraphQLQueryDefinition.class));

        GraphQLObjectType.Builder typeBuilder;
        typeBuilder = GraphQLObjectType.newObject()
                                       .definition(new ObjectTypeDefinition(QUERY_TYPE))
                                       .name(QUERY_TYPE);

        for (BeanDefinition bd : scanner.findCandidateComponents(GRAPHQL_BASE_PACKAGE)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                GraphQLObjectType type = GraphQLAnnotations.object(clazz);
                typeBuilder.fields(type.getFieldDefinitions());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        GraphQLSchema schema = GraphQLSchema.newSchema().query(typeBuilder).build();
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    public ExecutionResult execute(String query) {
        return execute(query, null);
    }

    public ExecutionResult execute(String query, Map<String, Object> variables) {

        Map<String, Object> notNullVars = variables != null ? variables : Collections.emptyMap();

        ExecutionInput input = ExecutionInput.newExecutionInput()
                                             .context(new GqlContext(serviceRegistry))
                                             .query(query)
                                             .variables(notNullVars)
                                             .build();

        ExecutionResult result = graphQL.execute(input);
        result = new GqlExecutionResult(result);

        if (logger.isWarnEnabled()) {
            for (GraphQLError error : result.getErrors()) {
                if (error instanceof ExceptionWhileDataFetching) {
                    ExceptionWhileDataFetching fetchExc = (ExceptionWhileDataFetching) error;
                    logger.warn("Exception while data fetching", fetchExc.getException());
                }
            }
        }

        return result;
    }

    @Override
    public ExecutionResult execute(RestTemplate template, String uri, String query, Map<String, Object> variables) {

        if (variables == null) {
            variables = Collections.emptyMap();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        GraphQLPost.Request request = new GraphQLPost.Request();
        request.query = query;
        request.variables = variables;

        HttpEntity<GraphQLPost.Request> requestEntity = new HttpEntity<>(request, headers);
        return parseRawResult(template.postForObject(uri, requestEntity, ObjectNode.class));
    }

    private ExecutionResult parseRawResult(ObjectNode resultNode) {
        JsonNode dataNode = resultNode.get("data");
        ArrayList<CiteckGraphQLException> errorsList = new ArrayList<>();
        ArrayNode errors = (ArrayNode) resultNode.get("errors");
        if (errors != null) {
            for (JsonNode error : errors) {
                errorsList.add(parseError((ObjectNode) error));
            }
        }
        return new ExecutionResultImpl(dataNode, errorsList);
    }

    private CiteckGraphQLException parseError(ObjectNode errorNode) {
        CiteckGraphQLException error = new CiteckGraphQLException();
        if (errorNode.has("message")) {
            error.setMessage(errorNode.get("message").asText());
        }
        if (errorNode.has("locations")) {
            ArrayNode rawLocations = (ArrayNode) errorNode.get("locations");
            List<SourceLocation> locations = new ArrayList<>(rawLocations.size());
            for (int i = 0; i < rawLocations.size(); i++) {
                JsonNode location = rawLocations.get(i);
                locations.add(new SourceLocation(
                        location.get("line").asInt(),
                        location.get("column").asInt()
                ));
            }
            error.setLocations(locations);
        }
        return error;
    }
}
