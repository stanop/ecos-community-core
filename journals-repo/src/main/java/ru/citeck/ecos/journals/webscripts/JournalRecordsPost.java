package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.search.CriteriaSearchResults;
import ru.citeck.ecos.search.CriteriaSearchService;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get journal records based on query in request body and journalId
 * Webscript is replacement for criteria-search.post which return too much information
 *
 * @author Pavel Simonov
 */
public class JournalRecordsPost extends AbstractWebScript {

    //========PARAMS========
    private static final String PARAM_JOURNAL_ID = "journalId";
    //=======/PARAMS========

    private static final String SEARCH_LANGUAGE = SearchService.LANGUAGE_FTS_ALFRESCO;

    private static final String GQL_PARAM_QUERY = "query";
    private static final String GQL_PARAM_LANGUAGE = "language";

    private static final String OPTION_DATASOURCE = "datasourceType";

    private static final String DATASOURCE_GRAPHQL = "graphql";
    private static final String DATASOURCE_CRITERIA_SEARCH = "criteria-search";

    private static final String KEY_RESULTS = "results";
    private static final String KEY_ATTRIBUTES = "attributes";
    private static final String KEY_DATA = "data";
    private static final String KEY_CRITERIA_SEARCH = "criteriaSearch";

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile("['\"]\\s*?(\\S+?:\\S+?\\s*?" +
            "(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]");
    private boolean newApiByDefault = false;

    private Properties globalProperties;
    private GraphQLService graphQLService;
    private JournalService journalService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private SearchCriteriaParser criteriaParser;
    private CriteriaSearchService criteriaSearchService;

    private String gqlBaseQuery;
    private Map<String, JournalRecordsQuery> gqlQueryByJournalId = new ConcurrentHashMap<>();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL_ID);

        if (StringUtils.isBlank(journalId)) {
            throw new RuntimeException("journalId is mandatory parameter");
        }

        JournalType journalType = journalService.getJournalType(journalId);
        if (journalType == null) {
            throw new RuntimeException(String.format("journal with id %s not found!", journalId));
        }

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

        String datasource = journalType.getOptions().get(OPTION_DATASOURCE);

        if (datasource == null) {
            datasource = newApiByDefault ? DATASOURCE_GRAPHQL : DATASOURCE_CRITERIA_SEARCH;
        }

        String criteriaQuery = req.getContent().getContent();

        if (datasource.equals(DATASOURCE_GRAPHQL)) {

            JournalRecordsQuery query =
                    gqlQueryByJournalId.computeIfAbsent(journalId, id -> generateGqlQuery(journalType));

            Map<String, Object> params = new HashMap<>();
            params.put(GQL_PARAM_QUERY, criteriaQuery);
            params.put(GQL_PARAM_LANGUAGE, SEARCH_LANGUAGE);

            Map<String, Object> queryResult = graphQLService.execute(query.query, params).toSpecification();
            Map<String, Object> result = new HashMap<>();

            Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) queryResult.get(KEY_DATA);
            Map<String, Object> resultData = data.get(KEY_CRITERIA_SEARCH);

            //map results to meet criteria-search format
            resultData.forEach((queryResultKey, queryResultValue) -> {

                if (queryResultKey.equals(KEY_RESULTS)) {

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> resultsList = (List<Map<String, Object>>) queryResultValue;
                    List<Map<String, Object>> newResultsList = new ArrayList<>();

                    for (Map<String, Object> record : resultsList) {

                        Map<String, Object> newRecord = new HashMap<>();
                        Map<String, Object> attributes = new HashMap<>();

                        record.forEach((recordKey, recordValue) -> {

                            Pair<String, String> mapping = query.attributesMapping.get(recordKey);
                            if (mapping != null) {
                                attributes.put(mapping.getFirst(), ((Map) recordValue).get(mapping.getSecond()));
                            } else {
                                newRecord.putIfAbsent(recordKey, recordValue);
                            }
                        });

                        newRecord.putIfAbsent(KEY_ATTRIBUTES, attributes);

                        newResultsList.add(newRecord);
                    }
                    result.put(KEY_RESULTS, newResultsList);

                } else {
                    result.put(queryResultKey, queryResultValue);
                }
            });

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(res.getOutputStream(), result);

        } else { //old behaviour from criteria-search.post

            SearchCriteria criteria = criteriaParser.parse(criteriaQuery);
            CriteriaSearchResults results = criteriaSearchService.query(criteria, SEARCH_LANGUAGE);

            Map<String, Object> model = new HashMap<>();
            model.put("query", results.getQuery());
            model.put("nodes", results.getResults());
            model.put("hasMore", results.hasMore());
            model.put("criteria", results.getCriteria());
            model.put("totalCount", results.getTotalCount());
            model.put("language", SEARCH_LANGUAGE);
            model.put("personFirstName", globalProperties.get("reportProducer.personFirstName"));
            model.put("personLastName", globalProperties.get("reportProducer.personLastName"));
            model.put("personMiddleName", globalProperties.get("reportProducer.personMiddleName"));
            model = createTemplateParameters(req, res, model);

            String templatePath = getDescription().getId() + ".json";
            renderTemplate(templatePath, model, res.getWriter());
        }

        res.setStatus(Status.STATUS_OK);
    }

    private JournalRecordsQuery generateGqlQuery(JournalType journalType) {

        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append(gqlBaseQuery).append(" ");

        schemaBuilder.append("fragment journalFields on GqlAlfNode {");
        schemaBuilder.append("nodeRef\n");
        schemaBuilder.append("isContainer\n");
        schemaBuilder.append("isDocument\n");

        Map<String, Pair<String, String>> attributesMapping = new HashMap<>();

        for (QName attribute : journalType.getAttributes()) {

            Map<String, String> attributeOptions = journalType.getAttributeOptions(attribute);
            String prefixedKey = attribute.toPrefixString(namespaceService);
            String underscoredKey = prefixedKey.replaceAll(":", "_");

            schemaBuilder.append(underscoredKey);
            schemaBuilder.append(": attribute(name:\"").append(prefixedKey).append("\"){");

            boolean attributeWithNodes;
            boolean isMultiple;

            PropertyDefinition propertyDef = dictionaryService.getProperty(attribute);
            if (propertyDef != null) {
                attributeWithNodes = propertyDef.getDataType().getName().equals(DataTypeDefinition.NODE_REF);
                isMultiple = propertyDef.isMultiValued();
            } else {
                AssociationDefinition assocDef = dictionaryService.getAssociation(attribute);
                if (assocDef != null) {
                    attributeWithNodes = true;
                    isMultiple = true;
                } else {
                    attributeWithNodes = false;
                    isMultiple = false;
                }
            }
            if (attributeWithNodes) {
                String nodeSchema = getAttributeNodeSchema(attributeOptions);
                schemaBuilder.append("nodes{").append(nodeSchema).append("}");
                attributesMapping.put(underscoredKey, new Pair<>(prefixedKey, "nodes"));
            } else {
                String attributeDataKey = isMultiple ? "values" : "value";
                schemaBuilder.append(attributeDataKey);
                attributesMapping.put(underscoredKey, new Pair<>(prefixedKey, attributeDataKey));
            }

            schemaBuilder.append("}");
        }

        schemaBuilder.append("}");

        return new JournalRecordsQuery(schemaBuilder.toString(), attributesMapping);
    }

    private String getAttributeNodeSchema(Map<String, String> attributeOptions) {
        String nodeSchema = attributeOptions.get("nodeSchema");
        if (StringUtils.isNotBlank(nodeSchema)) {
            return nodeSchema;
        }
        String formatter = attributeOptions.get("formatter");
        if (StringUtils.isNotBlank(formatter)) {
            Map<String, String> linkedNodeAttributes = new HashMap<>();
            if (formatter.contains("Link") || formatter.contains("nodeRef")) {
                linkedNodeAttributes.put("nodeRef", "nodeRef");
            }
            Matcher attrMatcher = FORMATTER_ATTRIBUTES_PATTERN.matcher(formatter);
            if (attrMatcher.find()) {
                do {
                    String attributes = attrMatcher.group(1);
                    for (String attr : attributes.split(",")) {
                        attr = attr.trim();
                        if (!linkedNodeAttributes.containsKey(attr)) {
                            String key = attr.replaceAll(":", "_");
                            linkedNodeAttributes.put(key, "attribute(name:\"" + attr + "\"){value}");
                        }
                    }
                } while (attrMatcher.find());
            } else {
                linkedNodeAttributes.put("displayName", "displayName");
            }
            StringBuilder sb = new StringBuilder();
            linkedNodeAttributes.forEach((k, v) -> sb.append(k).append(':').append(v).append(','));
            nodeSchema = sb.toString();
        } else {
            nodeSchema = "displayName";
        }
        return nodeSchema;
    }

    public boolean isNewApiByDefault() {
        return newApiByDefault;
    }

    public void setNewApiByDefault(boolean newApiByDefault) {
        this.newApiByDefault = newApiByDefault;
    }

    public void setGqlBaseQuery(String gqlBaseQuery) {
        this.gqlBaseQuery = gqlBaseQuery.trim();
    }

    public void clearCache() {
        gqlQueryByJournalId.clear();
    }

    public Map<String, String> getGqlQueryCache() {
        Map<String, String> result = new HashMap<>();
        gqlQueryByJournalId.forEach((k, v) -> result.put(k, v.query));
        return result;
    }

    @Autowired
    public void setCriteriaParser(SearchCriteriaParser criteriaParser) {
        this.criteriaParser = criteriaParser;
    }

    @Autowired
    public void setCriteriaSearchService(CriteriaSearchService criteriaSearchService) {
        this.criteriaSearchService = criteriaSearchService;
    }

    @Autowired
    @Qualifier("global-properties")
    public void setGlobalProperties(Properties properties) {
        this.globalProperties = properties;
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    @Autowired
    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        namespaceService = serviceRegistry.getNamespaceService();
        dictionaryService = serviceRegistry.getDictionaryService();
    }

    private static class JournalRecordsQuery {

        String query;
        Map<String, Pair<String, String>> attributesMapping;

        JournalRecordsQuery(String query, Map<String, Pair<String, String>> mapping) {
            this.query = query;
            this.attributesMapping = mapping;
        }
    }
}
