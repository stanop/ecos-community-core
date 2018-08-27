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

public class DefaultResponseConverter implements ResponseConverter {

    private static final String JOURNAL_RECORDS_PATH = "journalRecords";
    private static final String RECORD_PATH = "journalRecords.records";
    private static final String TOTAL_COUNT_PATH = "journalRecords.totalCount";
    private static final String PAGE_INFO_PATH = "journalRecords.pageInfo";
    private static final String HAS_NEXT_PAGE_PATH = "journalRecords.pageInfo.hasNextPage";
    private static final String SKIP_COUNT_PATH = "journalRecords.pageInfo.skipCount";
    private static final String MAX_ITEMS_PATH = "journalRecords.pageInfo.maxItems";

    private static final boolean DEFAULT_HAS_NEXT_PAGE = false;
    private static final int DEFAULT_SKIP_COUNT = 0;
    private static final int DEFAULT_MAX_ITEMS = 10;

    private static PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    @Override
    public JournalData convert(ExecutionResult source, Map<String, Object> additionalData) {
        JournalData journalData = new JournalData();

        //extensions block
        journalData.setExtensions(source.getExtensions());

        //errors block
        journalData.setErrors(source.getErrors());

        //data block
        if (source.getData() != null) {
            JournalData.Data data = getData(source);
            journalData.setData(data);
        }

        return journalData;
    }

    private JournalData.Data getData(ExecutionResult source) {
        JournalData.Data data = new JournalData.Data();

        JournalData.JournalRecords journalRecords = null;
        try {
            if (propertyUtilsBean.getProperty(source.getData(), JOURNAL_RECORDS_PATH) != null) {
                journalRecords = constructJournalRecords(source);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        data.setJournalRecords(journalRecords);
        return data;
    }

    private JournalData.JournalRecords constructJournalRecords(ExecutionResult source) {
        JournalData.JournalRecords journalRecords = new JournalData.JournalRecords();
        List<LinkedHashMap> records;
        try {
            records = (List<LinkedHashMap>) propertyUtilsBean.getProperty(source.getData(), RECORD_PATH);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            records = Collections.emptyList();
            e.printStackTrace();
        }
        journalRecords.setRecords(records);

        try {
            Number totalCount = ((Number) propertyUtilsBean.getProperty(source.getData(), TOTAL_COUNT_PATH));
            journalRecords.setTotalCount(totalCount != null ? totalCount.longValue() : records.size());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            if (propertyUtilsBean.getProperty(source.getData(), PAGE_INFO_PATH) != null) {
                JournalData.PageInfo pageInfo = constructPageInfo(source);
                journalRecords.setPageInfo(pageInfo);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return journalRecords;
    }


    private JournalData.PageInfo constructPageInfo(ExecutionResult source) {
        JournalData.PageInfo pageInfo = new JournalData.PageInfo();

        try {
            Boolean hasNextPage = (Boolean) propertyUtilsBean.getProperty(source.getData(), HAS_NEXT_PAGE_PATH);
            pageInfo.setHasNextPage(hasNextPage != null ? hasNextPage : DEFAULT_HAS_NEXT_PAGE);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            Integer skipCount = (Integer) propertyUtilsBean.getProperty(source.getData(), SKIP_COUNT_PATH);
            pageInfo.setSkipCount(skipCount != null ? skipCount : DEFAULT_SKIP_COUNT);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            Integer maxItems = (Integer) propertyUtilsBean.getProperty(source.getData(), MAX_ITEMS_PATH);
            pageInfo.setMaxItems(maxItems != null ? maxItems : DEFAULT_MAX_ITEMS);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            e.printStackTrace();
        }

        return pageInfo;
    }
}
