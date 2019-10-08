package ru.citeck.ecos.records.language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.querylang.QueryLangConverter;
import ru.citeck.ecos.querylang.QueryLangService;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.search.ftsquery.BinOperator;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.DictUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PredicateToFtsAlfrescoConverter implements QueryLangConverter {

    private static final Log logger = LogFactory.getLog(PredicateToFtsAlfrescoConverter.class);
    public static final String COMMA_DELIMER = ",";

    private DictUtils dictUtils;
    private SearchService searchService;
    private PredicateService predicateService;
    private NamespaceService namespaceService;
    private AssociationIndexPropertyRegistry associationIndexPropertyRegistry;

    @Autowired
    public PredicateToFtsAlfrescoConverter(DictUtils dictUtils,
                                           SearchService searchService,
                                           QueryLangService queryLangService,
                                           PredicateService predicateService,
                                           ServiceRegistry serviceRegistry,
                                           AssociationIndexPropertyRegistry associationIndexPropertyRegistry) {

        this.dictUtils = dictUtils;
        this.searchService = searchService;
        this.predicateService = predicateService;
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.associationIndexPropertyRegistry = associationIndexPropertyRegistry;

        queryLangService.register(this,
                PredicateService.LANGUAGE_PREDICATE,
                SearchService.LANGUAGE_FTS_ALFRESCO);
    }

    private void processPredicate(Predicate predicate, FTSQuery query) {

        if (predicate instanceof ComposedPredicate) {

            query.open();

            List<Predicate> predicates = ((ComposedPredicate) predicate).getPredicates();
            boolean isJoinByAnd = predicate instanceof AndPredicate;

            for (int i = 0; i < predicates.size(); i++) {
                if (i > 0) {
                    if (isJoinByAnd) {
                        query.and();
                    } else {
                        query.or();
                    }
                }

                processPredicate(predicates.get(i), query);
            }

            query.close();

        } else if (predicate instanceof NotPredicate) {

            query.not();
            processPredicate(((NotPredicate) predicate).getPredicate(), query);

        } else if (predicate instanceof ValuePredicate) {

            ValuePredicate valuePred = (ValuePredicate) predicate;
            String attribute = valuePred.getAttribute();
            Object value = valuePred.getValue();
            String valueStr = value.toString();

            switch (attribute) {
                case "PATH":

                    query.path(valueStr);

                    break;
                case "PARENT":
                case "_parent":

                    query.parent(new NodeRef(value.toString()));

                    break;
                case "TYPE":
                case "_type":

                    consumeQName(valueStr, query::type);

                    break;
                case "ASPECT":
                case "aspect":

                    consumeQName(valueStr, query::aspect);

                    break;
                case "ISNULL":

                    consumeQName(valueStr, query::isNull);

                    break;
                case "ISNOTNULL":

                    consumeQueryField(valueStr, query::isNotNull);

                    break;
                case "ISUNSET":

                    consumeQueryField(valueStr, query::isUnset);

                    break;
                default:

                    ClassAttributeDefinition attDef = dictUtils.getAttDefinition(attribute);
                    QName field = getQueryField(attDef);

                    if (field == null) {
                        break;
                    }

                    switch (valuePred.getType()) {
                        case EQ:
                            if (valueStr.contains(COMMA_DELIMER)) {
                                String[] values = valueStr.split(COMMA_DELIMER);
                                for (String s : values) {
                                    query.exact(field, BinOperator.OR, s);
                                }
                            } else {
                                query.exact(field, valueStr);
                            }
                            break;
                        case LIKE:
                            query.value(field, valueStr.replaceAll("%", "*"));
                            break;
                        case CONTAINS:

                            if (valueStr == null || valueStr.isEmpty()) {
                                return;
                            }

                            if (attDef instanceof PropertyDefinition) {

                                boolean textFlag = false;
                                DataTypeDefinition dataType = ((PropertyDefinition) attDef).getDataType();
                                if (dataType != null && (DataTypeDefinition.TEXT.equals(dataType.getName()) ||
                                                         DataTypeDefinition.MLTEXT.equals(dataType.getName())) ) {
                                    textFlag = true;
                                }
                                if (valueStr.contains(COMMA_DELIMER)) {
                                    String[] values = valueStr.split(COMMA_DELIMER);
                                    for (String s : values) {
                                        if (textFlag) {
                                            s = "*" + s + "*";
                                        }
                                        query.value(field, BinOperator.OR, s);
                                    }
                                } else {
                                    query.value(field, valueStr);
                                }

                            } else if (attDef instanceof AssociationDefinition) {

                                if (NodeRef.isNodeRef(valueStr)) {

                                    query.value(field, valueStr);

                                } else {

                                    //search assoc by text

                                    ClassDefinition targetClass = ((AssociationDefinition) attDef).getTargetClass();

                                    FTSQuery innerQuery = FTSQuery.createRaw();
                                    innerQuery.maxItems(20);
                                    innerQuery.type(targetClass.getName());

                                    String assocVal = "*" + valueStr + "*";

                                    Map<QName, Serializable> attributes = new HashMap<>();
                                    attributes.put(ContentModel.PROP_TITLE, assocVal);
                                    attributes.put(ContentModel.PROP_NAME, assocVal);

                                    Map<QName, PropertyDefinition> props = new HashMap<>(targetClass.getProperties());
                                    targetClass.getDefaultAspects(true)
                                               .forEach(a -> props.putAll(a.getProperties()));

                                    props.forEach((name, def) -> {

                                        QName dataType = def.getDataType().getName();

                                        if (DataTypeDefinition.TEXT.equals(dataType)
                                                || DataTypeDefinition.MLTEXT.equals(dataType)) {

                                            String ns = def.getName().getNamespaceURI();

                                            if (!ns.equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
                                                    && !ns.equals(NamespaceService.CONTENT_MODEL_1_0_URI)) {

                                                attributes.put(def.getName(), assocVal);
                                            }
                                        }
                                    });

                                    if (!attributes.isEmpty()) {

                                        innerQuery.and().values(attributes, BinOperator.OR, false);

                                        List<NodeRef> assocs = innerQuery.query(searchService);

                                        if (assocs.size() > 0) {
                                            query.any(field, new ArrayList<>(assocs));
                                        } else {
                                            query.value(field, valueStr);
                                        }

                                    } else {

                                        query.value(field, valueStr);
                                    }
                                }
                            }
                            break;
                        case GE:
                        case GT:
                        case LE:
                        case LT:

                            String predValue = null;
                            if (value instanceof String) {
                                if (attDef instanceof PropertyDefinition) {
                                    DataTypeDefinition type = ((PropertyDefinition) attDef).getDataType();
                                    if (DataTypeDefinition.DATETIME.equals(type.getName())) {
                                        predValue = convertTime(valueStr);
                                    }
                                }
                                predValue = "\"" + (predValue != null ? predValue : valueStr) + "\"";
                            } else {
                                predValue = valueStr;
                            }

                            switch (valuePred.getType()) {

                                case GE:
                                    query.range(field, predValue, true, null, false);
                                    break;
                                case GT:
                                    query.range(field, predValue, false, null, false);
                                    break;
                                case LE:
                                    query.range(field, null, false, predValue, true);
                                    break;
                                case LT:
                                    query.range(field, null, false, predValue, false);
                                    break;
                            }

                            break;
                        default:
                            throw new RuntimeException("Unknown value predicate type: " + valuePred.getType());
                    }
            }
        } else if (predicate instanceof EmptyPredicate) {

            String attribute = ((EmptyPredicate) predicate).getAttribute();
            consumeQueryField(attribute, query::empty);

        } else {
            throw new RuntimeException("Unknown predicate type: " + predicate);
        }
    }

    private String convertTime(String time) {

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
            logger.error(e);
            return time;
        }
    }

    private void consumeQueryField(String field, Consumer<QName> consumer) {
        QName attQName = getQueryField(dictUtils.getAttDefinition(field));
        if (attQName != null) {
            consumer.accept(attQName);
        }
    }

    private void consumeQName(String qname, Consumer<QName> consumer) {
        QName qName = QName.resolveToQName(namespaceService, qname);
        if (qName != null) {
            consumer.accept(qName);
        }
    }

    private QName getQueryField(ClassAttributeDefinition def) {
        if (def == null) {
            return null;
        }
        if (def instanceof AssociationDefinition) {
            return associationIndexPropertyRegistry.getAssociationIndexProperty(def.getName());
        }
        return def.getName();
    }

    @Override
    public JsonNode convert(JsonNode predicateQuery) {

        Predicate predicate = predicateService.readJson(predicateQuery);

        FTSQuery query = FTSQuery.createRaw();
        processPredicate(predicate, query);

        return TextNode.valueOf(query.getQuery());
    }
}
