package ru.citeck.ecos.graphql.journal.datasource;

import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.JGqlSortBy;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.journals.records.RecordsResult;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.*;

public class MultiDataSource implements JournalDataSource {

    private static final String DATASOURCE_NOT_SUPPORT_SPLIT_LOADING_ERROR_MESSAGE_PATTERN = "MultiDataSource can " +
            "works only with datasources that supports split loading. Problematic datasource is %s";

    private List<String> journalDataSources;

    @Override
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {
        return null;
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public RecordsResult queryIds(GqlContext context,
                                  String query,
                                  String language,
                                  JGqlPageInfoInput pageInfo) throws Exception {
        RecordsResult concatenatedRecordsResult = null;
        for (String dataSourceBeanName : journalDataSources) {
            JournalDataSource dataSource = findDataSource(dataSourceBeanName);
            if (!dataSource.isSupportsSplitLoading()) {
                throw new Exception(String.format(
                        DATASOURCE_NOT_SUPPORT_SPLIT_LOADING_ERROR_MESSAGE_PATTERN,
                        dataSourceBeanName));
            }
            JGqlPageInfoInput nextPageInfo = constructPageInfoForNextSearch(concatenatedRecordsResult, pageInfo);
            RecordsResult nextRecordsResult = dataSource.queryIds(context, query, language, nextPageInfo);
            concatenatedRecordsResult = concatenateRecordsResult(concatenatedRecordsResult, nextRecordsResult);
        }
        return concatenatedRecordsResult;
    }

    private RecordsResult concatenateRecordsResult(RecordsResult concatenatedRecordsResult, RecordsResult nextRecordsResult) {
        if (concatenatedRecordsResult == null && nextRecordsResult == null) {
            return new RecordsResult(Collections.emptyList(), false, 0, 0, 10);
        }

        if (concatenatedRecordsResult == null) {
            return nextRecordsResult;
        }

        if (nextRecordsResult == null) {
            return concatenatedRecordsResult;
        }

        long totalCount = sumTotalCount(concatenatedRecordsResult, nextRecordsResult);
        int maxItems = concatenatedRecordsResult.maxItems;
        int skipCount = concatenatedRecordsResult.skipCount;
        boolean hasNext = totalCount > (maxItems + skipCount);
        List<RemoteRef> records = concatenateRecordItems(
                concatenatedRecordsResult.records, nextRecordsResult.records, maxItems);

        return new RecordsResult(records, hasNext, totalCount, skipCount, maxItems);
    }

    private List<RemoteRef> concatenateRecordItems(List<RemoteRef> concatenatedRecords,
                                                   List<RemoteRef> nextRecords,
                                                   int maxItems) {
        if (isNullOrEmpty(concatenatedRecords) && isNullOrEmpty(nextRecords)) {
            return Collections.emptyList();
        }

        if (isNullOrEmpty(concatenatedRecords)) {
            return nextRecords;
        }

        if (isNullOrEmpty(nextRecords)) {
            return concatenatedRecords;
        }

        if (concatenatedRecords.size() >= maxItems) {
            return concatenatedRecords;
        }

        List<RemoteRef> result = new ArrayList<>(concatenatedRecords);
        for (RemoteRef remoteRef : nextRecords) {
            if (result.size() >= maxItems) {
                return result;
            }
            result.add(remoteRef);
        }

        return result;
    }

    private boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private long sumTotalCount(RecordsResult concatenatedRecordsResult, RecordsResult nextRecordsResult) {
        long concatenatedTotalCount = concatenatedRecordsResult.totalCount;
        long nextTotalCount = nextRecordsResult.totalCount;
        return concatenatedTotalCount + nextTotalCount;
    }

    @Override
    public List<JGqlAttributeValue> convertToGqlValue(GqlContext context,
                                                      List<RemoteRef> remoteRefList) {
        return null;
    }

    @Override
    public JournalData queryMetadata(String gqlQuery,
                                     String dataSourceBeanName,
                                     RecordsResult recordsResult) throws Exception {
        JournalData concatenatedJournalData = null;
        for (String dataSourceName : journalDataSources) {
            JournalDataSource dataSource = findDataSource(dataSourceName);
            if (!dataSource.isSupportsSplitLoading()) {
                throw new Exception(String.format(
                        DATASOURCE_NOT_SUPPORT_SPLIT_LOADING_ERROR_MESSAGE_PATTERN,
                        dataSourceBeanName));
            }
            RecordsResult nextRecordsResult = buildRecordsResultForCurrentServer(dataSource, recordsResult);
            JournalData nextJournalData = dataSource.queryMetadata(gqlQuery, dataSourceName, nextRecordsResult);
            concatenatedJournalData = concatenateJournalData(concatenatedJournalData, nextJournalData);
        }
        return concatenatedJournalData;
    }

    @Override
    public boolean isSupportsSplitLoading() {
        return true;
    }

    private RecordsResult buildRecordsResultForCurrentServer(JournalDataSource dataSource,
                                                             RecordsResult recordsResult) {
        String serverId = dataSource.getServerId();
        if (StringUtils.isBlank(serverId)) {
            serverId = "";
        }
        long totalCount = recordsResult.totalCount;
        int maxItems = recordsResult.maxItems;
        int skipCount = recordsResult.skipCount;
        boolean hasNext = recordsResult.hasNext;
        List<RemoteRef> records = new ArrayList<>();

        for (RemoteRef remoteRef : recordsResult.records) {
            if (serverId.equals(remoteRef.getServerId())) {
                records.add(remoteRef);
            }
        }

        return new RecordsResult(records, hasNext, totalCount, skipCount, maxItems);
    }

    private JGqlPageInfoInput constructPageInfoForNextSearch(RecordsResult concatenatedRecordsResult,
                                                             JGqlPageInfoInput pageInfoInput) {
        if (concatenatedRecordsResult == null) {
            return pageInfoInput;
        }

        String afterId = pageInfoInput.getAfterId();
        int maxItems = pageInfoInput.getMaxItems();
        List<JGqlSortBy> sortBy = pageInfoInput.getSortBy();
        int skipCount = pageInfoInput.getSkipCount();

        int upperBound = skipCount + maxItems;
        int difference = (int) (concatenatedRecordsResult.totalCount - upperBound);
        if (difference < 0) {
            int recordCountFromNextDatasource = (int) (upperBound - concatenatedRecordsResult.totalCount);
            int skipCountForFullList = recordCountFromNextDatasource - maxItems;
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

        long totalCount = concatenatedJournalRecords.getTotalCount();
        result.setTotalCount(totalCount);

        JournalData.PageInfo pageInfo = concatenatePageInfo(concatenatedJournalRecords.getPageInfo(),
                newJournalRecords.getPageInfo(), totalCount);
        result.setPageInfo(pageInfo);

        List<LinkedHashMap> records = concatenateRecords(concatenatedJournalRecords.getRecords(),
                newJournalRecords.getRecords(), pageInfo.getMaxItems());
        result.setRecords(records);

        return result;
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
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    public void setJournalDataSources(List<String> journalDataSources) {
        this.journalDataSources = journalDataSources;
    }
}
