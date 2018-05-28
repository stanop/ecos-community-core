package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.AlfGraphQLServiceImpl;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;

import java.io.IOException;
import java.io.Writer;
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
    private static final String GQL_PARAM_DATASOURCE = "datasource";
    private static final String GQL_PARAM_PAGE_INFO = "pageInfo";

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile("['\"]\\s*?(\\S+?:\\S+?\\s*?" +
                                                                                "(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]");

    private GraphQLService defaultGQLService;
    private JournalService journalService;
    private NamespaceService namespaceService;
    private ServiceRegistry serviceRegistry;

    private String gqlBaseQuery;
    private Map<String, String> gqlQueryByJournalId = new ConcurrentHashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL_ID);
        JournalType journalType = journalService.getJournalType(journalId);
        if (journalType == null) {
            try (Writer writer = res.getWriter()) {
                writer.write("Journal with id " + journalId + " not found");
            }
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }

        String gqlQuery = gqlQueryByJournalId.computeIfAbsent(journalId, id -> generateGqlQuery(journalType));

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        ExecutionResult executeResult = defaultGQLService.execute(gqlQuery, getParameters(req, journalType));
        objectMapper.writeValue(res.getOutputStream(), executeResult.toSpecification());

        res.setStatus(Status.STATUS_OK);
    }

    private String generateGqlQuery(JournalType journalType) {

        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append(gqlBaseQuery).append(" ");

        schemaBuilder.append("fragment recordsFields on JournalAttributeValueGql {");
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

        if (formatter.contains("Link") || formatter.contains("nodeRef")) {
            innerFields.add("id");
            innerFields.add("str");
        } else if (attributesToLoad.isEmpty()){
            innerFields.add("str");
        }

        for (String field : innerFields) {
            schemaBuilder.append(field).append(",");
        }

        schemaBuilder.append("}");

        return schemaBuilder.toString();
    }

    private Map<String, Object> getParameters(WebScriptRequest webRequest, JournalType journalType) {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put(GQL_PARAM_DATASOURCE, journalType.getDataSource());

        try {
            RequestBody request = objectMapper.readValue(webRequest.getContent().getContent(), RequestBody.class);
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
    @Qualifier("alfGraphQLServiceImpl")
    public void setDefaultGQLService(GraphQLService defaultGQLService) {
        this.defaultGQLService = defaultGQLService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        namespaceService = serviceRegistry.getNamespaceService();
        this.serviceRegistry = serviceRegistry;
    }

    private static class RequestBody {
        public String query;
        public Map<String, Object> pageInfo;
    }
}
