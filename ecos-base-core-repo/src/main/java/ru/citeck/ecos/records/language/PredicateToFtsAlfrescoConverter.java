package ru.citeck.ecos.records.language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.records2.QueryLangConverter;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.util.List;

@Component
public class PredicateToFtsAlfrescoConverter implements QueryLangConverter {

    private PredicateService predicateService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private AssociationIndexPropertyRegistry associationIndexPropertyRegistry;

    @Autowired
    public PredicateToFtsAlfrescoConverter(RecordsService recordsService,
                                           PredicateService predicateService,
                                           ServiceRegistry serviceRegistry,
                                           AssociationIndexPropertyRegistry associationIndexPropertyRegistry) {

        this.predicateService = predicateService;
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.associationIndexPropertyRegistry = associationIndexPropertyRegistry;

        recordsService.register(this,
                RecordsService.LANGUAGE_PREDICATE,
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

                    query.isNotNull(QName.resolveToQName(namespaceService, valueStr));

                    break;
                case "ISUNSET":

                    query.isUnset(QName.resolveToQName(namespaceService, valueStr));

                    break;
                default:

                    QName field = QName.resolveToQName(namespaceService, attribute);

                    AssociationDefinition assocDef = dictionaryService.getAssociation(field);
                    if (assocDef != null) {
                        field = associationIndexPropertyRegistry.getAssociationIndexProperty(field);
                    }

                    switch (valuePred.getType()) {
                        case EQ:
                            query.exact(field, valueStr);
                            break;
                        case LIKE:
                            query.value(field, valueStr.replaceAll("%", "*"));
                            break;
                        case CONTAINS:
                            if (assocDef == null) {
                                query.value(field, "*" + valueStr + "*");
                            } else {
                                query.value(field, valueStr);
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
            QName field = QName.resolveToQName(namespaceService, attribute);
            query.empty(field);

        } else {
            throw new RuntimeException("Unknown predicate type: " + predicate);
        }
    }

    @Override
    public JsonNode convert(JsonNode predicateQuery) {

        Predicate predicate = predicateService.readJson(predicateQuery);

        FTSQuery query = FTSQuery.createRaw();
        processPredicate(predicate, query);

        return TextNode.valueOf(query.getQuery());
    }
}
