package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfo;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;

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

    private static final String GQL_PARAM_QUERY = "query";
    private static final String GQL_PARAM_PAGE_INFO = "pageInfo";

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile("['\"]\\s*?(\\S+?:\\S+?\\s*?" +
                                                                                "(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]");

    private GraphQLService graphQLService;
    private JournalService journalService;
    private NamespaceService namespaceService;
    private ServiceRegistry serviceRegistry;

    private String gqlBaseQuery;
    private Map<String, String> gqlQueryByJournalId = new ConcurrentHashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL_ID);
        String gqlQuery = gqlQueryByJournalId.computeIfAbsent(journalId, id -> generateGqlQuery(journalId));

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        ExecutionResult executeResult = graphQLService.execute(gqlQuery, getParameters(req));
        objectMapper.writeValue(res.getOutputStream(), executeResult.toSpecification());

        res.setStatus(Status.STATUS_OK);
    }

    private String generateGqlQuery(String journalId) {

        JournalType journalType = journalService.getJournalType(journalId);
        QName dataSourceKey = QName.createQName(null, journalType.getDataSource());
        JournalDataSource dataSource = (JournalDataSource) serviceRegistry.getService(dataSourceKey);

        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append(gqlBaseQuery).append(" ");

        schemaBuilder.append("fragment recordsFields on JournalAttributeValueGql {");
        schemaBuilder.append("id\n");

        int attrCounter = 0;

        for (QName attribute : journalType.getAttributes()) {

            Map<String, String> attributeOptions = journalType.getAttributeOptions(attribute);
            String prefixedKey = attribute.toPrefixString(namespaceService);

            schemaBuilder.append("a")
                         .append(attrCounter++)
                         .append(":attr(name:\"")
                         .append(prefixedKey)
                         .append("\"){");

            JournalAttributeInfoGql info = dataSource.getAttributeInfo(prefixedKey).orElse(null);
            schemaBuilder.append(getAttributeSchema(attributeOptions, info));

            schemaBuilder.append("}");
        }

        schemaBuilder.append("}");

        return schemaBuilder.toString();
    }

    private String getAttributeSchema(Map<String, String> attributeOptions, JournalAttributeInfoGql info) {

        String schema = attributeOptions.get("attributeSchema");
        if (StringUtils.isNotBlank(schema)) {
            return "name,val{" + schema + "}";
        }

        String formatter = attributeOptions.get("formatter");
        formatter = formatter != null ? formatter : "";

        List<String> innerFields = new ArrayList<>();
        innerFields.add("disp");

        if (formatter.contains("Link") || formatter.contains("nodeRef")) {
            innerFields.add("id");
        }

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

        StringBuilder schemaBuilder = new StringBuilder("name,val{");

        for (String field : innerFields) {
            schemaBuilder.append(field).append(",");
        }

        int attrCounter = 0;
        for (String attrName : attributesToLoad) {
            schemaBuilder.append("a")
                         .append(attrCounter++)
                         .append(":attr(name:\"")
                         .append(attrName).append("\")")
                         .append("{name val}")
                         .append(",");
        }

        schemaBuilder.append("}");

        return schemaBuilder.toString();
    }

    private Map<String, Object> getParameters(WebScriptRequest webRequest) {

        Map<String, Object> parameters = new HashMap<>();

        for (String name : webRequest.getParameterNames()) {
            parameters.putIfAbsent(name, webRequest.getParameter(name));
        }

        try {
            Request request = objectMapper.readValue(webRequest.getContent().getContent(), Request.class);
            parameters.put(GQL_PARAM_QUERY, request.query);
            parameters.put(GQL_PARAM_PAGE_INFO, request.pageInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parameters;
    }

    public void setGqlBaseQuery(String gqlBaseQuery) {
        this.gqlBaseQuery = gqlBaseQuery.trim();
    }

    public void clearCache() {
        gqlQueryByJournalId.clear();
    }

    public Map<String, String> getGqlQueryCache() {
        return gqlQueryByJournalId;
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
        this.serviceRegistry = serviceRegistry;
    }

    private static class Request {
        public String query;
        public Map<String, Object> pageInfo;
    }
}
