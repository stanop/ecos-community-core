package ru.citeck.ecos.journals.records;

import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtilsBean;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverter;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverterFactory;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JournalRecordsDAO {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private ServiceRegistry serviceRegistry;
    private GqlQueryGenerator gqlQueryGenerator;
    private GqlQueryExecutor gqlQueryExecutor;

    private String recordsListPath;
    private String hasNextPagePath;
    private String totalCountPath;
    private String skipCountPath;
    private String maxItemsPath;

    private String recordsMetadataBaseQuery;
    private String recordsBaseQuery;
    private String gqlRecordsIdQuery;

    private PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
    private ResponseConverterFactory responseConverterFactory = new ResponseConverterFactory();

    public JournalData getRecordsWithData(JournalType journalType,
                                          String query,
                                          String language,
                                          JGqlPageInfoInput pageInfo) throws Exception {

        JournalDataSource dataSource = getDataSourceInstance(journalType);
        if (dataSource.isSupportsSplitLoading()) {
            GqlContext gqlContext = new GqlContext(serviceRegistry);
            RecordsResult recordsResult = dataSource.queryIds(gqlContext, query, language, pageInfo);
            String gqlQuery = gqlQueryGenerator.generate(journalType, recordsMetadataBaseQuery, dataSource);
            return dataSource.queryMetadata(gqlQuery, journalType.getDataSource(), recordsResult);
        } else {
            String gqlQuery = gqlQueryGenerator.generate(journalType, recordsBaseQuery, dataSource);
            ExecutionResult result = gqlQueryExecutor.executeQuery(journalType, gqlQuery,
                    query, language, pageInfo, dataSource);
            ResponseConverter responseConverter = responseConverterFactory.getConverter(dataSource);
            return responseConverter.convert(result, Collections.emptyMap());
        }
    }

    public RecordsResult getRecords(JournalType journalType,
                                    String query,
                                    String language,
                                    JGqlPageInfoInput pageInfo) {

        JournalDataSource dataSource = getDataSourceInstance(journalType);
        if (dataSource.isSupportsSplitLoading()) {
            GqlContext gqlContext = new GqlContext(serviceRegistry);
            try {
                return dataSource.queryIds(gqlContext, query, language, pageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ExecutionResult result = gqlQueryExecutor.executeQuery(journalType, gqlRecordsIdQuery,
                query, language, pageInfo, dataSource);

        List<Map<String, String>> recordsData = null;
        Boolean hasNextPage = null;
        Long totalCount = null;
        Integer skipCount = null;
        Integer maxItems = null;

        if (result.getData() != null) {
            try {
                Object recordsList = propertyUtilsBean.getProperty(result.getData(), recordsListPath);
                recordsData = recordsList instanceof List ? (List) recordsList : null;

                Object hasNextPageObj = propertyUtilsBean.getProperty(result.getData(), hasNextPagePath);
                if (hasNextPageObj instanceof Boolean) {
                    hasNextPage = (Boolean) hasNextPageObj;
                }

                Object totalCountObj = propertyUtilsBean.getProperty(result.getData(), totalCountPath);
                if (totalCountObj instanceof Long) {
                    totalCount = (Long) totalCountObj;
                }

                Object skipCountObj = propertyUtilsBean.getProperty(result.getData(), skipCountPath);
                if (skipCountObj instanceof Integer) {
                    skipCount = (Integer) skipCountObj;
                }

                Object maxItemsObj = propertyUtilsBean.getProperty(result.getData(), maxItemsPath);
                if (maxItemsObj instanceof Integer) {
                    maxItems = (Integer) maxItemsObj;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (recordsData == null) {
            recordsData = Collections.emptyList();
        }
        if (hasNextPage == null) {
            hasNextPage = pageInfo.getMaxItems() == recordsData.size();
        }
        if (totalCount == null) {
            totalCount = (long) recordsData.size();
        }
        if (skipCount == null) {
            skipCount = 0;
        }
        if (maxItems == null) {
            maxItems = DEFAULT_PAGE_SIZE;
        }

        List<RemoteRef> records = recordsData.stream()
                .map(entry -> new RemoteRef(entry.get("id")))
                .collect(Collectors.toList());

        return new RecordsResult(records, hasNextPage, totalCount, skipCount, maxItems);
    }

    private JournalDataSource getDataSourceInstance(JournalType journalType) {
        String dataSourceBeanId = journalType.getDataSource();
        QName dataSourceQname = QName.createQName(null, dataSourceBeanId);
        return (JournalDataSource) serviceRegistry.getService(dataSourceQname);
    }

    public void clearCache() {
        gqlQueryGenerator.clearCache();
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setGqlQueryGenerator(GqlQueryGenerator gqlQueryGenerator) {
        this.gqlQueryGenerator = gqlQueryGenerator;
    }

    public void setGqlQueryExecutor(GqlQueryExecutor gqlQueryExecutor) {
        this.gqlQueryExecutor = gqlQueryExecutor;
    }

    public void setRecordsBaseQuery(String recordsBaseQuery) {
        this.recordsBaseQuery = recordsBaseQuery;
        this.gqlRecordsIdQuery = recordsBaseQuery + "\nfragment recordsFields on JGqlAttributeValue { id }";
    }

    public void setRecordsMetadataBaseQuery(String recordsMetadataBaseQuery) {
        this.recordsMetadataBaseQuery = recordsMetadataBaseQuery;
    }

    public void setRecordsListPath(String recordsListPath) {
        this.recordsListPath = recordsListPath;
    }

    public void setHasNextPagePath(String hasNextPagePath) {
        this.hasNextPagePath = hasNextPagePath;
    }

    public void setTotalCountPath(String totalCountPath) {
        this.totalCountPath = totalCountPath;
    }

    public void setSkipCountPath(String skipCountPath) {
        this.skipCountPath = skipCountPath;
    }

    public void setMaxItemsPath(String maxItemsPath) {
        this.maxItemsPath = maxItemsPath;
    }
}
