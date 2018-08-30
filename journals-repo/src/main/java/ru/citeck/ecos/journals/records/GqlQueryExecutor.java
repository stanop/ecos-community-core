package ru.citeck.ecos.journals.records;

import graphql.ExecutionResult;
import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.datasource.RemoteJournalDataSource;
import ru.citeck.ecos.records.source.alfnode.CriteriaAlfNodesSearch;
import ru.citeck.ecos.journals.JournalType;

import java.util.HashMap;
import java.util.Map;

public class GqlQueryExecutor {

    public static final String GQL_PARAM_QUERY = "query";
    public static final String GQL_PARAM_LANGUAGE = "language";
    public static final String GQL_PARAM_PAGE_INFO = "pageInfo";
    public static final String GQL_PARAM_DATASOURCE = "datasource";
    public static final String GQL_PARAM_REMOTE_REFS = "remoteRefs";

    private GraphQLService graphQLService;

    public ExecutionResult executeQuery(JournalType journalType,
                                        String gqlQuery,
                                        String query,
                                        String language,
                                        JGqlPageInfoInput pageInfo,
                                        JournalDataSource dataSource) {

        String datasourceBeanName;
        if (StringUtils.isNotBlank(dataSource.getServerId())) {
            RemoteJournalDataSource remoteJournalDataSource = (RemoteJournalDataSource) dataSource;
            datasourceBeanName = remoteJournalDataSource.getRemoteDataSourceBeanName();
        } else {
            datasourceBeanName = journalType.getDataSource();
        }

        String validLanguage = StringUtils.isNotBlank(language) ? language : CriteriaAlfNodesSearch.LANGUAGE;

        Map<String, Object> params = new HashMap<>();
        params.put(GQL_PARAM_QUERY, query);
        params.put(GQL_PARAM_LANGUAGE, validLanguage);
        params.put(GQL_PARAM_PAGE_INFO, pageInfo);
        params.put(GQL_PARAM_DATASOURCE, datasourceBeanName);

        return graphQLService.execute(gqlQuery, params);
    }

    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }
}
