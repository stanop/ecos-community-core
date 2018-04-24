package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.schema.DataFetchingEnvironment;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLQueryDefinition;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.service.CiteckServices;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@GraphQLQueryDefinition
public class JournalGqlQueryDefinition {

    private static final Log logger = LogFactory.getLog(JournalGqlQueryDefinition.class);

    private static ConcurrentHashMap<String, Optional<JournalDataSource>> dataSources = new ConcurrentHashMap<>();

    @GraphQLField
    public static Optional<JournalGql> journal(DataFetchingEnvironment env,
                                               @GraphQLName("id") @GraphQLNonNull String id) {

        GqlContext context = env.getContext();
        JournalService journalService = context.getService(CiteckServices.JOURNAL_SERVICE);

        JournalType journal = journalService.getJournalType(id);

        Optional<JournalDataSource> dataSource = dataSources.computeIfAbsent(journal.getDataSource(), datasource -> {

            QName key = QName.createQName(null, datasource);
            Object datasourceBean = context.getService(key);

            if (datasourceBean == null) {
                logger.error("Journal datasource bean with id '" + datasource +
                             "' is not found for journal " + id);
            } else if (datasourceBean instanceof JournalDataSource) {
                return Optional.of((JournalDataSource) datasourceBean);
            } else {
                logger.error("Journal datasource doesn't implement interface JournalDataSource. " +
                             "journalId: " + id + " datasource id: " + datasource +
                             " DataSource class: " + datasourceBean.getClass().getName());
            }
            return Optional.empty();
        });

        return dataSource.map(journalDataSource -> new JournalGql(journal, context, journalDataSource));
    }

}
