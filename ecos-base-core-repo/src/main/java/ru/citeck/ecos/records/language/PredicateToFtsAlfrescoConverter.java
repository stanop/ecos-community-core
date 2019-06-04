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
import java.util.*;

@Component
public class PredicateToFtsAlfrescoConverter implements QueryLangConverter {

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
                case "PARENT":
                case "_parent":

                    query.parent(new NodeRef(value.toString()));

                    break;
                case "TYPE":
                case "_type":

                    query.type(QName.resolveToQName(namespaceService, valueStr));

                    break;
                case "ASPECT":

                    query.aspect(QName.resolveToQName(namespaceService, valueStr));

                    break;
                case "ISNULL":

                    query.isNull(QName.resolveToQName(namespaceService, valueStr));

                    break;
                case "ISNOTNULL":

                    query.isNotNull(getQueryField(dictUtils.getAttDefinition(valueStr)));

                    break;
                case "ISUNSET":

                    query.isUnset(getQueryField(dictUtils.getAttDefinition(valueStr)));

                    break;
                default:

                    ClassAttributeDefinition attDef = dictUtils.getAttDefinition(attribute);
                    QName field = getQueryField(attDef);

                    switch (valuePred.getType()) {
                        case EQ:
                            query.exact(field, valueStr);
                            break;
                        case LIKE:
                            query.value(field, valueStr.replaceAll("%", "*"));
                            break;
                        case CONTAINS:

                            if (valueStr == null || valueStr.isEmpty()) {
                                return;
                            }

                            if (attDef instanceof PropertyDefinition) {

                                DataTypeDefinition dataType = ((PropertyDefinition) attDef).getDataType();
                                if (dataType != null && (DataTypeDefinition.TEXT.equals(dataType.getName()) ||
                                                         DataTypeDefinition.MLTEXT.equals(dataType.getName())) ) {
                                    query.value(field, "*" + valueStr + "*");
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

                            String predValue;
                            if (value instanceof String) {
                                predValue = "\"" + valueStr + "\"";
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
            query.empty(getQueryField(dictUtils.getAttDefinition(attribute)));

        } else {
            throw new RuntimeException("Unknown predicate type: " + predicate);
        }
    }

    private QName getQueryField(ClassAttributeDefinition def) {
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
