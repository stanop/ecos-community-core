package ru.citeck.ecos.journals.records;

import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.graphql.AlfGraphQLServiceImpl;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.MetadataExecutionResult;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JournalRecordsDAO {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile(
            "['\"]\\s*?(\\S+?:\\S+?\\s*?(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]"
    );

    private GraphQLService graphQLService;
    private ServiceRegistry serviceRegistry;
    private NamespaceService namespaceService;

    private ConcurrentHashMap<String, String> gqlQueryWithDataByJournalId = new ConcurrentHashMap<>();

    private String recordsListPath;
    private String hasNextPagePath;
    private String totalCountPath;
    private String skipCountPath;
    private String maxItemsPath;

    private String recordsMetadataBaseQuery;
    private String recordsBaseQuery;
    private String gqlRecordsIdQuery;

    private PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    public ExecutionResult getRecordsWithData(JournalType journalType,
                                              String query,
                                              String language,
                                              JGqlPageInfoInput pageInfo) {

        JournalDataSource dataSource = getDataSourceInstance(journalType);

        if (dataSource.isSupportsSplitLoading()) {
            GqlContext gqlContext = new GqlContext(serviceRegistry);
            RecordsResult recordsResult = dataSource.queryIds(gqlContext, query, language, pageInfo);

            String gqlQuery = gqlQueryWithDataByJournalId.computeIfAbsent(journalType.getId(),
                    id -> generateGqlQueryWithData(journalType, recordsMetadataBaseQuery));
            ExecutionResult metadata = dataSource.queryMetadata(journalType, gqlQuery, recordsResult.records);

            Map<String, Object> paginationData = constructPaginationDataMap(recordsResult);
            return new MetadataExecutionResult(metadata, paginationData);
        } else {
            String gqlQuery = gqlQueryWithDataByJournalId.computeIfAbsent(journalType.getId(),
                    id -> generateGqlQueryWithData(journalType, recordsBaseQuery));

            return executeQuery(journalType, gqlQuery, query, language, pageInfo);
        }
    }

    public RecordsResult getRecords(JournalType journalType,
                                    String query,
                                    String language,
                                    JGqlPageInfoInput pageInfo) {

        JournalDataSource dataSource = getDataSourceInstance(journalType);
        if (dataSource.isSupportsSplitLoading()) {
            GqlContext gqlContext = new GqlContext(serviceRegistry);
            return dataSource.queryIds(gqlContext, query, language, pageInfo);
        }

        ExecutionResult result = executeQuery(journalType, gqlRecordsIdQuery, query, language, pageInfo);

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

    private ExecutionResult executeQuery(JournalType journalType,
                                         String gqlQuery,
                                         String query,
                                         String language,
                                         JGqlPageInfoInput pageInfo) {

        String datasource = journalType.getDataSource();
        String validLanguage = StringUtils.isNotBlank(language) ? language : CriteriaAlfNodesSearch.LANGUAGE;

        Map<String, Object> params = new HashMap<>();
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_QUERY, query);
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_LANGUAGE, validLanguage);
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_PAGE_INFO, pageInfo);
        params.put(AlfGraphQLServiceImpl.GQL_PARAM_DATASOURCE, datasource);

        return graphQLService.execute(gqlQuery, params);
    }

    private Map<String, Object> constructPaginationDataMap(RecordsResult recordsResult) {
        Map<String, Object> paginationData = new HashMap<>();
        paginationData.put(MetadataExecutionResult.PAGINATION_MAX_ITEMS_KEY, recordsResult.maxItems);
        paginationData.put(MetadataExecutionResult.PAGINATION_SKIP_COUNT_KEY, recordsResult.skipCount);
        paginationData.put(MetadataExecutionResult.PAGINATION_TOTAL_COUNT_KEY, recordsResult.totalCount);
        paginationData.put(MetadataExecutionResult.PAGINATION_HAS_NEXT_PAGE_KEY, recordsResult.hasNext);
        return paginationData;
    }

    private JournalDataSource getDataSourceInstance(JournalType journalType) {
        String dataSourceBeanId = journalType.getDataSource();
        QName dataSourceQname = QName.createQName(null, dataSourceBeanId);
        return (JournalDataSource) serviceRegistry.getService(dataSourceQname);
    }

    private String generateGqlQueryWithData(JournalType journalType, String baseQuery) {

        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append(baseQuery).append(" ");

        schemaBuilder.append("fragment recordsFields on JGqlAttributeValue {");
        schemaBuilder.append("id\n");

        int attrCounter = 0;

        QName dataSourceKey = QName.createQName(null, journalType.getDataSource());
        JournalDataSource dataSource = (JournalDataSource) serviceRegistry.getService(dataSourceKey);

        List<QName> attributes = new ArrayList<>(journalType.getAttributes());
        for (String defaultAttr : dataSource.getDefaultAttributes()) {
            attributes.add(QName.resolveToQName(namespaceService, defaultAttr));
        }

        for (QName attribute : attributes) {

            Map<String, String> attributeOptions = journalType.getAttributeOptions(attribute);
            String prefixedKey = attribute.toPrefixString(namespaceService);

            schemaBuilder.append("a")
                    .append(attrCounter++)
                    .append(":attr(name:\"")
                    .append(prefixedKey)
                    .append("\"){");

            JGqlAttributeInfo info = dataSource.getAttributeInfo(prefixedKey).orElse(null);
            schemaBuilder.append(getAttributeSchema(attributeOptions, info));

            schemaBuilder.append("}");
        }

        schemaBuilder.append("}");

        return schemaBuilder.toString();
    }

    private String getAttributeSchema(Map<String, String> attributeOptions, JGqlAttributeInfo info) {

        String schema = attributeOptions.get("attributeSchema");
        if (StringUtils.isNotBlank(schema)) {
            return "name,val{" + schema + "}";
        }

        String formatter = attributeOptions.get("formatter");
        formatter = formatter != null ? formatter : "";

        StringBuilder schemaBuilder = new StringBuilder("name,val{");

        // attributes

        Set<String> attributesToLoad = new HashSet<>();
        if (info != null) {
            attributesToLoad.addAll(info.getDefaultInnerAttributes());
        }

        Matcher attrMatcher = FORMATTER_ATTRIBUTES_PATTERN.matcher(formatter);
        if (attrMatcher.find()) {
            do {
                String attributes = attrMatcher.group(1);
                for (String attr : attributes.split(",")) {
                    attributesToLoad.add(attr.trim());
                }
            } while (attrMatcher.find());
        }

        if (formatter.contains("typeName")) {
            attributesToLoad.add("classTitle");
        }

        int attrCounter = 0;
        for (String attrName : attributesToLoad) {
            schemaBuilder.append("a")
                    .append(attrCounter++)
                    .append(":attr(name:\"")
                    .append(attrName).append("\")")
                    .append("{name val{str}}")
                    .append(",");
        }

        // inner fields
        List<String> innerFields = new ArrayList<>();

        QName dataType = info != null ? info.getDataType() : DataTypeDefinition.ANY;
        boolean isNode = dataType.equals(DataTypeDefinition.NODE_REF);
        boolean isQName = dataType.equals(DataTypeDefinition.QNAME);

        if (formatter.contains("Link") || formatter.contains("nodeRef")) {
            innerFields.add("id");
            innerFields.add("str");
        } else if (attributesToLoad.isEmpty() || (!isNode && !isQName)) {
            innerFields.add("str");
        }

        for (String field : innerFields) {
            schemaBuilder.append(field).append(",");
        }

        schemaBuilder.append("}");

        return schemaBuilder.toString();
    }

    public void clearCache() {
        gqlQueryWithDataByJournalId.clear();
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    @Autowired
    @Qualifier("alfGraphQLServiceImpl")
    public void setGQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
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
