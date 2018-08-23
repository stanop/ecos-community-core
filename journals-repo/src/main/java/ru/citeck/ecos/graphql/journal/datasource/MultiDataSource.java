package ru.citeck.ecos.graphql.journal.datasource;

import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JGqlSortBy;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverter;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverterFactory;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.records.GqlQueryExecutor;
import ru.citeck.ecos.journals.records.GqlQueryGenerator;
import ru.citeck.ecos.journals.records.RecordsResult;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.*;

public class MultiDataSource implements JournalDataSource {

    private List<String> journalDataSources;

    private ServiceRegistry serviceRegistry;
    private GqlQueryGenerator gqlQueryGenerator;
    private GqlQueryExecutor gqlQueryExecutor;

    private String gqlAllRecordsQuery;
    private String gqlMetadataRecordsQuery;

    private ResponseConverterFactory responseConverterFactory = new ResponseConverterFactory();

    @Override
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {
        return null;
    }

    @Override
    public GraphQLService getGraphQLService() {
        return null;
    }

    @Override
    public String getRemoteDataSourceBeanName() {
        return null;
    }

    @Override
    public RecordsResult queryIds(GqlContext context,
                                  String query,
                                  String language,
                                  JGqlPageInfoInput pageInfo) {
        return null;
    }

    @Override
    public List<JGqlAttributeValue> convertToGqlValue(GqlContext context,
                                                      List<RemoteRef> remoteRefList) {
        return null;
    }

    @Override
    public JournalData queryMetadata(String dataSourceBeanName,
                                     String gqlQuery,
                                     RecordsResult recordsResult) {
        return null;
    }

    @Override
    public JournalData queryFromMultipleSources(JournalType journalType,
                                                String query,
                                                String language,
                                                JGqlPageInfoInput pageInfoInput) {
        JournalData concatenatedJournalData = null;
        for (String dataSourceName : journalDataSources) {
            JournalDataSource dataSource = findDataSource(dataSourceName);
            JournalData newJournalData;
            JGqlPageInfoInput nextPageInfo = constructPageInfoForNextSearch(concatenatedJournalData, pageInfoInput);
            if (dataSource.isSupportsSplitLoading()) {
                GqlContext context = new GqlContext(serviceRegistry);
                RecordsResult recordsResult = dataSource.queryIds(context, query, language, nextPageInfo);
                String gqlQuery = gqlQueryGenerator.generate(journalType, gqlMetadataRecordsQuery, dataSource);
                newJournalData = dataSource.queryMetadata(dataSourceName, gqlQuery, recordsResult);
            } else {
                String gqlQuery = gqlQueryGenerator.generate(journalType, gqlAllRecordsQuery, dataSource);
                ExecutionResult result = gqlQueryExecutor.executeQuery(journalType, gqlQuery,
                        query, language, nextPageInfo, dataSourceName, dataSource);
                ResponseConverter converter = responseConverterFactory.getConverter(dataSource);
                newJournalData = converter.convert(result, Collections.emptyMap());
            }
            concatenatedJournalData = concatenateJournalData(concatenatedJournalData, newJournalData);
        }
        return concatenatedJournalData;
    }

    private JGqlPageInfoInput constructPageInfoForNextSearch(JournalData concatenatedJournalData,
                                                             JGqlPageInfoInput pageInfoInput) {
        if (concatenatedJournalData == null ||
                concatenatedJournalData.getData() == null ||
                concatenatedJournalData.getData().getJournalRecords() == null ||
                concatenatedJournalData.getData().getJournalRecords().getPageInfo() == null) {
            return pageInfoInput;
        }

        JournalData.JournalRecords journalRecords = concatenatedJournalData.getData().getJournalRecords();

        String afterId = pageInfoInput.getAfterId();
        int maxItems = pageInfoInput.getMaxItems();
        List<JGqlSortBy> sortBy = pageInfoInput.getSortBy();
        int skipCount = pageInfoInput.getSkipCount();

        int upperBound = skipCount + maxItems;
        int difference = (int) (journalRecords.getTotalCount() - upperBound);
        if (difference < 0) {
            int recordsCountFromNextDatasource = (int) (upperBound - journalRecords.getTotalCount());
            int skipCountForFullList = recordsCountFromNextDatasource - maxItems;
            skipCount = skipCountForFullList >= 0 ? skipCountForFullList : 0;
        }

        return new JGqlPageInfoInput(afterId, maxItems, sortBy, skipCount);
    }

    private JournalDataSource findDataSource(String dataSourceName) {
        return (JournalDataSource) ApplicationContextProvider.getBean(dataSourceName);
    }

    private JournalData concatenateJournalData(JournalData concatenatedJournalData,
                                               JournalData newJournalData) {
        if (concatenatedJournalData == null && newJournalData == null) {
            return new JournalData();
        }

        if (concatenatedJournalData == null) {
            return newJournalData;
        }

        if (newJournalData == null) {
            return concatenatedJournalData;
        }

        JournalData.Data dataBlock = concatenateDataBlock(
                concatenatedJournalData.getData(), newJournalData.getData());
        Object errorsBlock = concatenatedJournalData.getErrors();
        Map<Object, Object> extensionsBlock = concatenatedJournalData.getExtensions();

        JournalData result = new JournalData();
        result.setData(dataBlock);
        result.setErrors(errorsBlock);
        result.setExtensions(extensionsBlock);
        return result;
    }

    private JournalData.Data concatenateDataBlock(JournalData.Data concatenatedData,
                                                  JournalData.Data newData) {
        if (concatenatedData == null && newData == null) {
            return new JournalData.Data();
        }

        if (concatenatedData == null) {
            return newData;
        }

        if (newData == null) {
            return concatenatedData;
        }

        JournalData.Data result = new JournalData.Data();
        JournalData.JournalRecords journalRecords = concatenateJournalRecords(
                concatenatedData.getJournalRecords(), newData.getJournalRecords());
        result.setJournalRecords(journalRecords);
        return result;
    }

    private JournalData.JournalRecords concatenateJournalRecords(JournalData.JournalRecords concatenatedJournalRecords,
                                                                 JournalData.JournalRecords newJournalRecords) {
        if (concatenatedJournalRecords == null && newJournalRecords == null) {
            return new JournalData.JournalRecords();
        }

        if (concatenatedJournalRecords == null) {
            return newJournalRecords;
        }

        if (newJournalRecords == null) {
            return concatenatedJournalRecords;
        }

        JournalData.JournalRecords result = new JournalData.JournalRecords();

        long totalCount = sumTotalCount(concatenatedJournalRecords, newJournalRecords);
        result.setTotalCount(totalCount);

        JournalData.PageInfo pageInfo = concatenatePageInfo(concatenatedJournalRecords.getPageInfo(),
                newJournalRecords.getPageInfo(), totalCount);
        result.setPageInfo(pageInfo);

        List<LinkedHashMap> records = concatenateRecords(concatenatedJournalRecords.getRecords(),
                newJournalRecords.getRecords(), pageInfo.getMaxItems());
        result.setRecords(records);

        return result;
    }

    private long sumTotalCount(JournalData.JournalRecords concatenatedJournalRecords,
                               JournalData.JournalRecords newJournalRecords) {
        long concatenatedTotalCount = concatenatedJournalRecords.getTotalCount();
        long newTotalCount = newJournalRecords.getTotalCount();
        return concatenatedTotalCount + newTotalCount;
    }

    private JournalData.PageInfo concatenatePageInfo(JournalData.PageInfo concatenatedPageInfo,
                                                     JournalData.PageInfo newPageInfo,
                                                     long totalCount) {
        if (concatenatedPageInfo == null && newPageInfo == null) {
            return new JournalData.PageInfo();
        }

        if (concatenatedPageInfo == null) {
            return newPageInfo;
        }

        if (newPageInfo == null) {
            return concatenatedPageInfo;
        }

        JournalData.PageInfo result = new JournalData.PageInfo();
        result.setMaxItems(concatenatedPageInfo.getMaxItems());
        result.setSkipCount(concatenatedPageInfo.getSkipCount());

        boolean hasNextPage = totalCount > (result.getMaxItems() + result.getSkipCount());
        result.setHasNextPage(hasNextPage);
        return result;
    }

    private List<LinkedHashMap> concatenateRecords(List<LinkedHashMap> concatenatedList,
                                                   List<LinkedHashMap> newList,
                                                   int maxItems) {
        if (concatenatedList == null && newList == null) {
            return new ArrayList<>();
        }

        if (concatenatedList == null) {
            return newList;
        }

        if (newList == null) {
            return concatenatedList;
        }

        if (concatenatedList.size() >= maxItems) {
            return concatenatedList;
        }

        List<LinkedHashMap> result = new ArrayList<>(concatenatedList);
        for (LinkedHashMap record : newList) {
            if (result.size() >= maxItems) {
                return result;
            }
            result.add(record);
        }
        return result;
    }

    @Override
    public boolean isMultiDataSource() {
        return true;
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    public void setJournalDataSources(List<String> journalDataSources) {
        this.journalDataSources = journalDataSources;
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

    public void setGqlAllRecordsQuery(String gqlAllRecordsQuery) {
        this.gqlAllRecordsQuery = gqlAllRecordsQuery;
    }

    public void setGqlMetadataRecordsQuery(String gqlMetadataRecordsQuery) {
        this.gqlMetadataRecordsQuery = gqlMetadataRecordsQuery;
    }
}
