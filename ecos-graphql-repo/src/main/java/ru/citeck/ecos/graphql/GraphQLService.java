package ru.citeck.ecos.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;

@Service
public class GraphQLService {

    private static final String GRAPHQL_BASE_PACKAGE = "ru.citeck.ecos.graphql";
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
        return execute(query, Collections.emptyMap());
    }

    public ExecutionResult execute(String query, Map<String, Object> variables) {
        ExecutionInput input = ExecutionInput.newExecutionInput()
                                             .context(new GqlContext(serviceRegistry))
                                             .query(query)
                                             .variables(variables)
                                             .build();
        return graphQL.execute(input);
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
