package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalRecordsConnection;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@GraphQLQueryDefinition
public class JournalGqlQueryDefinition {

    private static final Log logger = LogFactory.getLog(JournalGqlQueryDefinition.class);

    private static ConcurrentHashMap<String, Optional<JournalDataSource>> dataSources = new ConcurrentHashMap<>();

    @GraphQLField
    public static Optional<JournalRecordsConnection> journalRecords(DataFetchingEnvironment env,
                                                                    @GraphQLName("datasource") String datasource,
                                                                    @GraphQLName("language") String language,
                                                                    @GraphQLName("query") String query,
                                                                    @GraphQLName("pageInfo")
                                                                                JournalGqlPageInfoInput pageInfo) {
        GqlContext context = env.getContext();

        Optional<JournalDataSource> dataSource = dataSources.computeIfAbsent(datasource, source -> {

            QName key = QName.createQName(null, source);
            Object datasourceBean = context.getService(key);

            if (datasourceBean == null) {
                logger.error("Journal datasource bean with id '" + source + "' is not found");
            } else if (datasourceBean instanceof JournalDataSource) {
                return Optional.of((JournalDataSource) datasourceBean);
            } else {
                logger.error("Journal datasource doesn't implement interface JournalDataSource. " +
                             " datasource id: " + source +
                             " DataSource class: " + datasourceBean.getClass().getName());
            }
            return Optional.empty();
        });

        return dataSource.map(source -> source.getRecords(context, query, language, pageInfo));
    }
}
