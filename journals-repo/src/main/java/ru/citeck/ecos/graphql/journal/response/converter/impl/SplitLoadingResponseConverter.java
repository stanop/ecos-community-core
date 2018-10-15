package ru.citeck.ecos.graphql.journal.response.converter.impl;

import graphql.ExecutionResult;
import org.apache.commons.beanutils.PropertyUtilsBean;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.journal.response.converter.ResponseConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SplitLoadingResponseConverter implements ResponseConverter {

    private static PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    private static final String RECORD_PATH = "journalRecordsMetadata";
    private static final Integer DEFAULT_PAGE_SIZE = 10;

    public static final String PAGINATION_HAS_NEXT_PAGE_KEY = "hasNextPage";
    public static final String PAGINATION_SKIP_COUNT_KEY = "skipCount";
    public static final String PAGINATION_MAX_ITEMS_KEY = "maxItems";
    public static final String PAGINATION_TOTAL_COUNT_KEY = "totalCount";

    @Override
    public JournalData convert(ExecutionResult source, Map<String, Object> additionalData) {
        if (additionalData == null) additionalData = Collections.emptyMap();
        JournalData journalData = new JournalData();

        //extensions block
        journalData.setExtensions(source.getExtensions());

        //errors block
        journalData.setErrors(source.getErrors());

        //data block
        if (source.getData() != null) {
            JournalData.Data data = getData(source, additionalData);
            journalData.setData(data);
        }

        return journalData;
    }

    private JournalData.Data getData(ExecutionResult source, Map<String, Object> additionalData) {
        JournalData.Data data = new JournalData.Data();

        JournalData.JournalRecords journalRecords = new JournalData.JournalRecords();

        List<?> recordMap = null;
        try {
            recordMap = (List<?>) propertyUtilsBean.getProperty(source.getData(), RECORD_PATH);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            e.printStackTrace();
        }
        journalRecords.setRecords(recordMap);

        long totalCount = ((Number) additionalData.getOrDefault(PAGINATION_TOTAL_COUNT_KEY, 0L)).longValue();
        journalRecords.setTotalCount(totalCount);

        JournalData.PageInfo pageInfo = constructPageInfo(additionalData);
        journalRecords.setPageInfo(pageInfo);

        data.setJournalRecords(journalRecords);
        return data;
    }

    private JournalData.PageInfo constructPageInfo(Map<String, Object> additionalData) {
        JournalData.PageInfo pageInfo = new JournalData.PageInfo();
        pageInfo.setHasNextPage((Boolean) additionalData.getOrDefault(PAGINATION_HAS_NEXT_PAGE_KEY, false));
        pageInfo.setMaxItems((Integer) additionalData.getOrDefault(PAGINATION_MAX_ITEMS_KEY, DEFAULT_PAGE_SIZE));
        pageInfo.setSkipCount((Integer) additionalData.getOrDefault(PAGINATION_SKIP_COUNT_KEY, 0));
        return pageInfo;
    }
}
