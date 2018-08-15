package ru.citeck.ecos.graphql;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class MetadataExecutionResult extends GqlExecutionResult {

    public static final String PAGINATION_HAS_NEXT_PAGE_KEY = "hasNextPage";
    public static final String PAGINATION_SKIP_COUNT_KEY = "skipCount";
    public static final String PAGINATION_MAX_ITEMS_KEY = "maxItems";
    public static final String PAGINATION_TOTAL_COUNT_KEY = "totalCount";
    public static final String PAGINATION_PAGE_INFO_KEY = "pageInfo";
    public static final String RECORDS_KEY = "records";
    public static final String JOURNAL_RECORDS_METADATA_PROPERTY = "journalRecordsMetadata";
    public static final String DATA_PROPERTY = "data";
    public static final String ERRORS_PROPERTY = "errors";
    public static final String EXTENSIONS_PROPERTY = "extensions";
    public static final String JOURNAL_RECORDS_PROPERTY = "journalRecords";

    private Map<String, Object> paginationData;

    public MetadataExecutionResult(ExecutionResult rawResult, Map<String, Object> paginationData) {
        super(rawResult);
        this.paginationData = paginationData;
    }

    @Override
    public Map<String, Object> toSpecification() {
        Map<String, Object> result = new LinkedHashMap<>();

        Object data = getData();
        if (data != null) {
            result.put(DATA_PROPERTY, dataToSpec());
        }
        if (getErrors() != null && !getErrors().isEmpty()) {
            result.put(ERRORS_PROPERTY, errorsToSpec(getErrors()));
        }
        Map<Object, Object> extensions = getExtensions();
        if (extensions != null) {
            result.put(EXTENSIONS_PROPERTY, extensions);
        }

        return result;
    }

    private Map<String, Object> dataToSpec() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put(JOURNAL_RECORDS_PROPERTY, getJournalRecordsToSpec());

        return result;
    }

    private Map<String, Object> getJournalRecordsToSpec() {
        Map<String, Object> journalRecords = new LinkedHashMap<>();

        List records = getRecords();
        if (records != null) {
            journalRecords.put(RECORDS_KEY, records);
        }
        Long totalCount = (Long) paginationData.getOrDefault(PAGINATION_TOTAL_COUNT_KEY, null);
        if (totalCount != null) {
            journalRecords.put(PAGINATION_TOTAL_COUNT_KEY, totalCount);
        }
        Map<String, Object> pageInfo = constructPageInfoMap();
        if (pageInfo != null) {
            journalRecords.put(PAGINATION_PAGE_INFO_KEY, pageInfo);
        }

        return journalRecords;
    }

    private Map<String, Object> constructPageInfoMap() {
        Map<String, Object> pageInfo = new LinkedHashMap<>();

        Boolean hasNextPage = (Boolean) paginationData.getOrDefault(PAGINATION_HAS_NEXT_PAGE_KEY, null);
        if (hasNextPage != null) {
            pageInfo.put(PAGINATION_HAS_NEXT_PAGE_KEY, hasNextPage);
        }
        Integer skipCount = (Integer) paginationData.getOrDefault(PAGINATION_SKIP_COUNT_KEY, null);
        if (skipCount != null) {
            pageInfo.put(PAGINATION_SKIP_COUNT_KEY, skipCount);
        }
        Integer maxItems = (Integer) paginationData.getOrDefault(PAGINATION_MAX_ITEMS_KEY, null);
        if (maxItems != null) {
            pageInfo.put(PAGINATION_MAX_ITEMS_KEY, maxItems);
        }

        return pageInfo;
    }

    private Object errorsToSpec(List<GraphQLError> errors) {
        return errors.stream().map(GraphQLError::toSpecification).collect(toList());
    }

    private List getRecords() {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        List records;
        try {
            records = (List) propertyUtilsBean.getProperty(getData(), JOURNAL_RECORDS_METADATA_PROPERTY);
        } catch (Exception e) {
            records = Collections.emptyList();
            e.printStackTrace();
        }
        return records;
    }
}
