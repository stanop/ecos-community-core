package ru.citeck.ecos.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.*;
import graphql.language.SourceLocation;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.graphql.exceptions.CiteckGraphQLException;
import ru.citeck.ecos.remote.RestConnection;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphQLServiceImpl implements GraphQLService {

    private static final Log logger = LogFactory.getLog(GraphQLServiceImpl.class);

    private static final String TXN_GQL_CONTEXT_KEY = GraphQLServiceImpl.class.getName() + ".context";

    @Autowired
    private ServiceRegistry serviceRegistry;

    private List<GqlTypeDefinition> graphQLTypes;

    private GraphQL graphQL;

    @PostConstruct
    void init() {

        Map<String, GraphQLObjectType.Builder> types = new HashMap<>();
        graphQLTypes.forEach(def -> {

            GraphQLObjectType type = def.getType();
            if (type == null) {
                logger.warn("Type definition return nothing: " + def.getClass());
                return;
            }
            GraphQLObjectType.Builder builder = types.get(type.getName());

            if (builder == null) {
                builder = GraphQLObjectType.newObject(type);
                types.put(type.getName(), builder);
            } else {
                builder.fields(type.getFieldDefinitions());
            }
        });

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        types.values().forEach(t -> {
            GraphQLObjectType type = t.build();
            if (type.getName().equals(QUERY_TYPE)) {
                schemaBuilder.query(type);
            } else {
                schemaBuilder.additionalType(type);
            }
        });
        GraphQLSchema graphQLSchema = schemaBuilder.build();

        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    public ExecutionResult execute(String query) {
        return execute(query, null);
    }

    public ExecutionResult execute(String query, Map<String, Object> variables) {
        return executeImpl(query, variables, getGqlContext());
    }

    @Override
    public ExecutionResult execute(String query, Map<String, Object> variables, Object context) {
        return executeImpl(query, variables, context);
    }

    private ExecutionResult executeImpl(String query, Map<String, Object> variables, Object context) {

        Map<String, Object> notNullVars = variables != null ? variables : Collections.emptyMap();

        ExecutionInput input = ExecutionInput.newExecutionInput()
                                             .context(context)
                                             .query(query)
                                             .variables(notNullVars)
                                             .build();

        ExecutionResult result = graphQL.execute(input);
        result = new GqlExecutionResult(result);

        List<GraphQLError> errors = result.getErrors();

        if (errors != null && !errors.isEmpty()) {

            logger.error("GraphQL query completed with errors:\nQuery: " + query + "\nvariables: " + variables);

            for (GraphQLError error : errors) {

                List<SourceLocation> locations = error.getLocations();
                String locationsMsg = "";
                if (locations != null && locations.size() > 0) {
                    locationsMsg = " at " + locations.stream()
                            .map(l -> l.getLine() + ":" + l.getColumn())
                            .collect(Collectors.joining(", ")) + " ";
                }

                String message = "GraphQL " + error.getErrorType() + locationsMsg + "message: " + error.getMessage();

                if (error instanceof ExceptionWhileDataFetching) {
                    logger.error(message, ((ExceptionWhileDataFetching) error).getException());
                } else {
                    logger.error(message);
                }
            }
        }

        return result;
    }

    public AlfGqlContext getGqlContext() {
        AlfrescoTransactionSupport.TxnReadState readState = AlfrescoTransactionSupport.getTransactionReadState();
        AlfGqlContext context;
        if (AlfrescoTransactionSupport.TxnReadState.TXN_READ_ONLY.equals(readState)) {
            context = AlfrescoTransactionSupport.getResource(TXN_GQL_CONTEXT_KEY);
            if (context == null) {
                context = new AlfGqlContext(serviceRegistry);
                AlfrescoTransactionSupport.bindResource(TXN_GQL_CONTEXT_KEY, context);
            }
        } else {
            context = new AlfGqlContext(serviceRegistry);
        }
        return context;
    }

    @Override
    public ExecutionResult execute(RestConnection restConn,
                                   String url,
                                   String query,
                                   Map<String, Object> variables) {

        if (variables == null) {
            variables = Collections.emptyMap();
        }

        GraphQLPost.Request request = new GraphQLPost.Request();
        request.query = query;
        request.variables = variables;

        ObjectNode result = restConn.jsonPost(url, request, ObjectNode.class);
        if (result == null) {
            logger.error("restConn.jsonPost result is null! " +
                         "Seems that remote request was failed. Return null. " +
                         "Connection: " + restConn + " URL: " + url +
                         " Query: " + query + " variables: " + variables);
            return null;
        }
        return parseRawResult(result);
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

    @Autowired
    public void setGraphQLTypes(List<GqlTypeDefinition> graphQLTypes) {
        this.graphQLTypes = graphQLTypes;
    }
}
