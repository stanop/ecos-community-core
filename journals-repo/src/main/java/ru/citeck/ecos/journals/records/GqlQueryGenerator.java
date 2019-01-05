package ru.citeck.ecos.journals.records;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.source.MetaAttributeDef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GqlQueryGenerator {

    private static final Pattern FORMATTER_ATTRIBUTES_PATTERN = Pattern.compile(
            "['\"]\\s*?(\\S+?:\\S+?\\s*?(,\\s*?\\S+?:\\S+?\\s*?)*?)['\"]"
    );

    private NamespaceService namespaceService;
    private RecordsService recordsService;

    private ConcurrentHashMap<String, String> gqlQueryWithDataByJournalId = new ConcurrentHashMap<>();

    public String generate(JournalType journalType) {
        return gqlQueryWithDataByJournalId.computeIfAbsent(journalType.getId(),
                id -> generateGqlQueryWithData(journalType));
    }

    private String generateGqlQueryWithData(JournalType journalType) {

        String dataSource = journalType.getDataSource();

        StringBuilder schemaBuilder = new StringBuilder();

        schemaBuilder.append("id\n");

        int attrCounter = 0;

        List<QName> attributes = new ArrayList<>(journalType.getAttributes());
        if (StringUtils.isEmpty(journalType.getDataSource())) {
            attributes.add(AttributeModel.ATTR_ASPECTS);
            attributes.add(AttributeModel.ATTR_IS_CONTAINER);
            attributes.add(AttributeModel.ATTR_IS_DOCUMENT);
        }

        Set<String> strAtts = attributes.stream()
                                        .map(a -> a.toPrefixString(namespaceService))
                                        .collect(Collectors.toSet());

        List<MetaAttributeDef> defs = recordsService.getAttsDefinition(dataSource, strAtts);

        Map<String, MetaAttributeDef> defsByName = new HashMap<>();
        for (MetaAttributeDef def : defs) {
            defsByName.put(def.getName(), def);
        }

        for (QName attribute : attributes) {

            Map<String, String> attributeOptions = journalType.getAttributeOptions(attribute);
            String prefixedKey = attribute.toPrefixString(namespaceService);

            schemaBuilder.append("a")
                    .append(attrCounter++)
                    .append(":edge(n:\"")
                    .append(prefixedKey)
                    .append("\"){");

            MetaAttributeDef info = defsByName.get(prefixedKey);
            schemaBuilder.append(getAttributeSchema(attributeOptions, info));

            schemaBuilder.append("}");
        }

        return schemaBuilder.toString();
    }

    private String getAttributeSchema(Map<String, String> attributeOptions, MetaAttributeDef info) {

        String schema = attributeOptions.get("attributeSchema");
        if (StringUtils.isNotBlank(schema)) {
            return "name,val:vals{" + schema + "}";
        }

        String formatter = attributeOptions.get("formatter");
        formatter = formatter != null ? formatter : "";

        StringBuilder schemaBuilder = new StringBuilder("name,val:vals{");

        // attributes
        Set<String> attributesToLoad = new HashSet<>();
        if (info != null) {
            if (QName.class.isAssignableFrom(info.getDataType())) {
                attributesToLoad.add("shortName");
            }
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
                    .append(":edge(n:\"")
                    .append(attrName).append("\")")
                    .append("{name val:vals{str}}")
                    .append(",");
        }

        // inner fields
        List<String> innerFields = new ArrayList<>();

        Class dataType = info != null ? info.getDataType() : Object.class;
        boolean isNode = NodeRef.class.isAssignableFrom(dataType);
        boolean isQName = QName.class.isAssignableFrom(dataType);

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
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }
}
