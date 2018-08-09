package ru.citeck.ecos.journals.records;

import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JournalRecordsDAO {

    private static final String GQL_PARAM_QUERY = "query";
    private static final String GQL_PARAM_LANGUAGE = "language";
    private static final String GQL_PARAM_PAGE_INFO = "pageInfo";
    private static final String GQL_PARAM_DATASOURCE = "datasource";

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile(
            "['\"]\\s*?(\\S+?:\\S+?\\s*?(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]"
    );

    private ServiceRegistry serviceRegistry;
    private NamespaceService namespaceService;

    private ConcurrentHashMap<String, String> gqlQueryWithDataByJournalId = new ConcurrentHashMap<>();

    private String recordsListPath;
    private String hasNextPagePath;
    private String totalCountPath;

    private String recordsBaseQuery;
    private String gqlRecordsIdQuery;

    private PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    public ExecutionResult getRecordsWithData(JournalType journalType,
                                              String query,
                                              String language,
                                              JGqlPageInfoInput pageInfo) {

        String gqlQuery = gqlQueryWithDataByJournalId.computeIfAbsent(journalType.getId(),
                                                                      id -> generateGqlQueryWithData(journalType));

        return executeQuery(journalType, gqlQuery, query, language, pageInfo);
    }

    public RecordsResult getRecords(JournalType journalType,
                                    String query,
                                    String language,
                                    JGqlPageInfoInput pageInfo) {

        ExecutionResult result = executeQuery(journalType, gqlRecordsIdQuery, query, language, pageInfo);

        List<Map<String, String>> recordsData = null;
        Boolean hasNextPage = null;
        Long totalCount = null;

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

        List<RemoteRef> records = recordsData.stream()
                .map(entry -> new RemoteRef(entry.get("id")))
                .collect(Collectors.toList());

        return new RecordsResult(records, hasNextPage, totalCount);
    }

    private ExecutionResult executeQuery(JournalType journalType,
                                         String gqlQuery,
                                         String query,
                                         String language,
                                         JGqlPageInfoInput pageInfo) {

        String graphQLServiceBeanId = journalType.getGraphQLService();
        GraphQLService graphQLService = (GraphQLService) ApplicationContextProvider.getBean(graphQLServiceBeanId);

        String datasource = journalType.getDataSource();
        String validLanguage = StringUtils.isNotBlank(language) ? language : CriteriaAlfNodesSearch.LANGUAGE;

        Map<String, Object> params = new HashMap<>();
        params.put(GQL_PARAM_QUERY, query);
        params.put(GQL_PARAM_LANGUAGE, validLanguage);
        params.put(GQL_PARAM_PAGE_INFO, pageInfo);
        params.put(GQL_PARAM_DATASOURCE, datasource);

        return graphQLService.execute(gqlQuery, params);
    }

    private String generateGqlQueryWithData(JournalType journalType) {

        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append(recordsBaseQuery).append(" ");

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

    public void setRecordsBaseQuery(String recordsBaseQuery) {
        this.recordsBaseQuery = recordsBaseQuery;
        this.gqlRecordsIdQuery = recordsBaseQuery + "\nfragment recordsFields on JGqlAttributeValue { id }";
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
}
