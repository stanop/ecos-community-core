package ru.citeck.ecos.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.*;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.language.ObjectTypeDefinition;
import graphql.language.SourceLocation;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.graphql.exceptions.CiteckGraphQLException;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.remote.RestConnection;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GraphQLServiceImpl implements GraphQLService {

    private static final Log logger = LogFactory.getLog(GraphQLServiceImpl.class);

    private static final String TXN_GQL_CONTEXT_KEY = GraphQLServiceImpl.class.getName() + ".context";
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
        return executeImpl(query, variables, getContext());
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
            logger.error("connection.jsonPost result is null! " +
                         "Seems that remote request was failed. Return null. " +
                         "Connection: " + restConn + " URL: " + url + " Query: " + query + " variables: " + variables);
            return null;
        }
        return parseRawResult(result);
    }

    @Override
    public ExecutionResult execute(String query,
                                   Map<String, Object> variables,
                                   Function<GqlContext, List<MetaValue>> valuesProvider) {

        GqlContext context = getContext();
        context.setMetaValues(valuesProvider.apply(context));

        return executeImpl(query, variables, context);
    }

    private ExecutionResult executeImpl(String query, Map<String, Object> variables, GqlContext context) {

        Map<String, Object> notNullVars = variables != null ? variables : Collections.emptyMap();

        ExecutionInput input = ExecutionInput.newExecutionInput()
                                             .context(context)
                                             .query(query)
                                             .variables(notNullVars)
                                             .build();

        ExecutionResult result = graphQL.execute(input);
        result = new GqlExecutionResult(result);

        if (logger.isWarnEnabled()) {
            for (GraphQLError error : result.getErrors()) {

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

    private GqlContext getContext() {
        AlfrescoTransactionSupport.TxnReadState readState = AlfrescoTransactionSupport.getTransactionReadState();
        GqlContext context;
        if (AlfrescoTransactionSupport.TxnReadState.TXN_READ_ONLY.equals(readState)) {
            context = AlfrescoTransactionSupport.getResource(TXN_GQL_CONTEXT_KEY);
            if (context == null) {
                context = new GqlContext(serviceRegistry);
                AlfrescoTransactionSupport.bindResource(TXN_GQL_CONTEXT_KEY, context);
            }
        } else {
            context = new GqlContext(serviceRegistry);
        }
        return context;
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
