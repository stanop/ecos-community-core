package ru.citeck.ecos.journals.records;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.journals.JournalType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GqlQueryGenerator {

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile(
            "['\"]\\s*?(\\S+?:\\S+?\\s*?(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]"
    );

    private ServiceRegistry serviceRegistry;
    private NamespaceService namespaceService;

    private ConcurrentHashMap<String, String> gqlQueryWithDataByJournalId = new ConcurrentHashMap<>();


    public String generate(JournalType journalType, String baseQuery, JournalDataSource dataSource) {
        return gqlQueryWithDataByJournalId.computeIfAbsent(journalType.getId(),
                id -> generateGqlQueryWithData(journalType, baseQuery, dataSource));
    }


    private String generateGqlQueryWithData(JournalType journalType, String baseQuery, JournalDataSource dataSource) {

        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append(baseQuery).append(" ");

        schemaBuilder.append("fragment recordsFields on JGqlAttributeValue {");
        schemaBuilder.append("id\n");

        int attrCounter = 0;

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

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

}
