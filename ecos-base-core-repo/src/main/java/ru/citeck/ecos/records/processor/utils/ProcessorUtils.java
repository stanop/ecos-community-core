package ru.citeck.ecos.records.processor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.Predicate;
import ru.citeck.ecos.querylang.QueryLangConverter;
import ru.citeck.ecos.querylang.QueryLangService;
import ru.citeck.ecos.records.language.PredicateToFtsAlfrescoConverter;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.DictUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Slf4j
@Component
public class ProcessorUtils implements QueryLangConverter {

    private NamespaceService namespaceService;
    private PredicateService predicateService;
    private AssociationIndexPropertyRegistry associationIndexPropertyRegistry;
    private DictUtils dictUtils;
    private PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter;

    @Autowired
    public ProcessorUtils(NamespaceService namespaceService,
                          PredicateService predicateService,
                          AssociationIndexPropertyRegistry associationIndexPropertyRegistry,
                          DictUtils dictUtils,
                          QueryLangService queryLangService) {
        this.namespaceService = namespaceService;
        this.predicateService = predicateService;
        this.associationIndexPropertyRegistry = associationIndexPropertyRegistry;
        this.dictUtils = dictUtils;

        queryLangService.register(this,
                PredicateService.LANGUAGE_PREDICATE,
                SearchService.LANGUAGE_FTS_ALFRESCO);
    }

    public boolean isNodeRefAtt(ClassAttributeDefinition attDef) {

        if (attDef == null) {
            return false;
        }

        if (attDef instanceof PropertyDefinition) {
            PropertyDefinition propDef = (PropertyDefinition) attDef;
            DataTypeDefinition dataType = propDef.getDataType();
            if (dataType != null) {
                return DataTypeDefinition.NODE_REF.equals(dataType.getName());
            } else {
                return false;
            }
        } else {
            return attDef instanceof AssociationDefinition;
        }

    }

    public String toValidNodeRef(String value) {

        int idx = value.lastIndexOf("@workspace://");
        if (idx > -1 && idx < value.length() - 1) {
            value = value.substring(idx + 1);
        }
        return value;
    }

    public String convertTime(String time) {

        if (time == null || time.charAt(time.length() - 1) != 'Z') {
            return time;
        }

        ZoneOffset offset = OffsetDateTime.now().getOffset();
        if (offset.getTotalSeconds() == 0) {
            return time;
        }

        try {
            Instant timeInstant = Instant.parse(time);
            return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(timeInstant.atZone(offset));
        } catch (Exception e) {
            log.error("Parse time", e);
            return time;
        }
    }

    public void consumeQueryField(String field, Consumer<QName> consumer) {
        QName attQName = getQueryField(dictUtils.getAttDefinition(field));
        if (attQName != null) {
            consumer.accept(attQName);
        }
    }

    public void consumeQName(String qname, Consumer<QName> consumer) {
        QName qName = QName.resolveToQName(namespaceService, qname);
        if (qName != null) {
            consumer.accept(qName);
        }
    }

    @Override
    public JsonNode convert(JsonNode predicateQuery) {

        Predicate predicate = predicateService.readJson(predicateQuery);

        FTSQuery query = FTSQuery.createRaw();
        predicateToFtsAlfrescoConverter.processPredicate(predicate, query);

        return TextNode.valueOf(query.getQuery());
    }

    public QName getQueryField(ClassAttributeDefinition def) {
        if (def == null) {
            return null;
        }
        if (def instanceof AssociationDefinition) {
            return associationIndexPropertyRegistry.getAssociationIndexProperty(def.getName());
        }
        return def.getName();
    }

    @Autowired
    public void setPredicateToFtsAlfrescoConverter(PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter) {
        this.predicateToFtsAlfrescoConverter = predicateToFtsAlfrescoConverter;
    }

}
