package ru.citeck.ecos.graphql.journal.datasource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JGqlSortBy;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.SortBy;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;
import ru.citeck.ecos.records.source.RecordsDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;
import ru.citeck.ecos.search.SortOrder;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * This records DAO required for backward compatibility. Don't use it for new data sources
 * @see RecordsDAO
 *
 * @deprecated implement RecordsDAO instead
 */
public class JournalDatasourceRecordsDAO extends AbstractRecordsDAO implements RecordsDAO, RecordsWithMetaDAO {

    private ServiceRegistry serviceRegistry;
    private GraphQLService graphQLService;
    private JournalDataSource dataSource;
    private GqlMetaUtils metaUtils;

    @PostConstruct
    public void init() {
        Object datasouce = serviceRegistry.getService(QName.createQName("", getId()));
        if (datasouce == null) {
            throw new IllegalStateException("Datasource " + getId() + " is not found!");
        }
        if (datasouce instanceof JournalDataSource) {
            dataSource = (JournalDataSource) datasouce;
        } else {
            String typeName = datasouce.getClass().getName();
            throw new IllegalStateException("Datasource bean \"" + getId() +
                                            "\" has incorrect type. Class: " + typeName);
        }
    }

    @Override
    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema) {

        String metaQuery = metaUtils.createQuery("metaValues", metaSchema);

        List<JGqlSortBy> sortBy = new ArrayList<>();
        for (SortBy sort: query.getSortBy()) {
            String order = (sort.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING).getValue();
            sortBy.add(new JGqlSortBy(sort.getAttribute(), order));
        }

        RecordRef afterId = query.getAfterId();
        JGqlPageInfoInput pageInfo = new JGqlPageInfoInput(afterId != null ? afterId.getId() : null,
                                                           query.getMaxItems(),
                                                           sortBy,
                                                           query.getSkipCount());

        RecordsResult<ObjectNode> result = new RecordsResult<>();

        ExecutionResult gqlResult = graphQLService.execute(metaQuery, null, context -> {
            JGqlRecordsConnection records = dataSource.getRecords(context,
                                                                  query.getQuery(),
                                                                  query.getLanguage(),
                                                                  pageInfo);
            result.setTotalCount(records.totalCount());
            result.setHasMore(records.pageInfo().isHasNextPage());
            return records.records();
        });

        result.setRecords(metaUtils.convertMeta(gqlResult));

        return result;
    }

    @Autowired
    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    @Autowired
    public void setMetaUtils(GqlMetaUtils metaUtils) {
        this.metaUtils = metaUtils;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
