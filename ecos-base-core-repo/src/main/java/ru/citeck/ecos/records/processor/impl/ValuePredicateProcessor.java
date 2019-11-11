package ru.citeck.ecos.records.processor.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.model.ComposedPredicate;
import ru.citeck.ecos.predicate.model.OrPredicate;
import ru.citeck.ecos.predicate.model.ValuePredicate;
import ru.citeck.ecos.records.language.PredicateToFtsAlfrescoConverter;
import ru.citeck.ecos.records.processor.utils.AttributeConstants;
import ru.citeck.ecos.records.processor.PredicateProcessor;
import ru.citeck.ecos.records.processor.exception.UnknownValuePredicateTypeException;
import ru.citeck.ecos.records.processor.utils.ProcessorUtils;
import ru.citeck.ecos.search.ftsquery.BinOperator;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.DictUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static ru.citeck.ecos.predicate.model.ValuePredicate.Type.*;

@Component
public class ValuePredicateProcessor implements PredicateProcessor<ValuePredicate> {

    private static final String COMMA_DELIMER = ",";

    private PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter;
    private final ProcessorUtils processorUtils;
    private final DictUtils dictUtils;
    private final SearchService searchService;

    @Autowired
    public void setPredicateToFtsAlfrescoConverter(PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter) {
        this.predicateToFtsAlfrescoConverter = predicateToFtsAlfrescoConverter;
    }

    @Autowired
    public ValuePredicateProcessor(ProcessorUtils processorUtils,
                                   DictUtils dictUtils,
                                   SearchService searchService) {
        this.processorUtils = processorUtils;
        this.dictUtils = dictUtils;
        this.searchService = searchService;
    }

    @Override
    public void process(ValuePredicate predicate, FTSQuery query) {

        String valueStr = predicate.getValue().toString();
        String attribute = predicate.getAttribute();

        switch (attribute) {
            case AttributeConstants.ATTR_PATH:
                query.path(valueStr);
                break;

            case AttributeConstants.ATTR_PARENT:
            case AttributeConstants.ATTR_PARENT_ENDING:
                String nodeRefStr = processorUtils.toValidNodeRef(valueStr);
                NodeRef nodeRef = new NodeRef(nodeRefStr);
                query.parent(nodeRef);
                break;

            case AttributeConstants.ATTR_TYPE:
            case AttributeConstants.ATTR_TYPE_ENDING:
                processorUtils.consumeQName(valueStr, query::type);
                break;

            case AttributeConstants.ATTR_ASPECT:
            case AttributeConstants.ATTR_ASPECT_LOWCASE:
                processorUtils.consumeQName(valueStr, query::aspect);
                break;

            case AttributeConstants.ATTR_IS_NULL:
                processorUtils.consumeQName(valueStr, query::isNull);
                break;

            case AttributeConstants.ATTR_IS_NOT_NULL:
                processorUtils.consumeQueryField(valueStr, query::isNotNull);
                break;

            case AttributeConstants.ATTR_IS_UNSET:
                processorUtils.consumeQueryField(valueStr, query::isUnset);
                break;

            default:
                processOtherAttributes(predicate, query);
                break;
        }
    }

    private void handleMultipleValues(ValuePredicate predicate, FTSQuery query) {
        String[] values = predicate.getValue().toString().split(COMMA_DELIMER);
        ComposedPredicate orPredicate = new OrPredicate();
        for (String s : values) {
            orPredicate.addPredicate(new ValuePredicate(predicate.getAttribute(), predicate.getType(), s));
        }
        predicateToFtsAlfrescoConverter.processPredicate(orPredicate, query);

    }

    /*
     *   TODO: REWORK AND DIVIDE LOGIC OF THIS METHOD AND METHODS BELOW
     *
     *   NOTE: We must reworked this method!
     *         It's too huge and that difficult to add new logic here or edit existing.
     * */
    private void processOtherAttributes(ValuePredicate predicate, FTSQuery query) {
        String attribute = predicate.getAttribute();
        Object value = predicate.getValue();
        String valueStr = value.toString();

        ClassAttributeDefinition attDef = dictUtils.getAttDefinition(attribute);
        QName field = processorUtils.getQueryField(attDef);

        if (field == null) {
            return;
        }

        // accepting multiple values by comma
        if (valueStr.contains(COMMA_DELIMER) &&
                (predicate.getType().equals(EQ) || predicate.getType().equals(CONTAINS))) {
            handleMultipleValues(predicate, query);
            return;
        }

        if (processorUtils.isNodeRefAtt(attDef)) {
            valueStr = processorUtils.toValidNodeRef(valueStr);
        }

        switch (predicate.getType()) {
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
                    QName typeName = dataType != null ? dataType.getName() : null;

                    if (DataTypeDefinition.TEXT.equals(typeName) ||
                            DataTypeDefinition.MLTEXT.equals(typeName)) {

                        // here need to search aliases of 'valueStr' and then use 'any'.

                        query.value(field, "*" + valueStr + "*");

                    } else if (DataTypeDefinition.CATEGORY.equals(typeName)) {

                        searchAssocByText(predicate, query);

                    } else {
                        query.value(field, valueStr);
                    }

                } else if (attDef instanceof AssociationDefinition) {

                    if (NodeRef.isNodeRef(valueStr)) {

                        query.value(field, valueStr);

                    } else {

                        searchAssocByText(predicate, query);

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
                            predValue = processorUtils.convertTime(valueStr);
                        }
                    }
                    predValue = "\"" + (predValue != null ? predValue : valueStr) + "\"";
                } else {
                    predValue = valueStr;
                }

                switch (predicate.getType()) {

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
                throw new UnknownValuePredicateTypeException(predicate.getType());
        }
    }

    private void searchAssocByText(ValuePredicate predicate, FTSQuery query) {

        String attribute = predicate.getAttribute();
        Object value = predicate.getValue();
        String valueStr = value.toString();

        ClassAttributeDefinition attDef = dictUtils.getAttDefinition(attribute);
        QName field = processorUtils.getQueryField(attDef);

        FTSQuery innerQuery = FTSQuery.createRaw();
        innerQuery.maxItems(20);

        ClassDefinition targetClass = null;
        if (attDef instanceof AssociationDefinition) {

            targetClass = ((AssociationDefinition) attDef).getTargetClass();
            innerQuery.type(targetClass.getName());
        } else {

            QName targetName = ((PropertyDefinition) attDef).getDataType().getName();
            if (targetName.getLocalName().equals("category")) {
                innerQuery.type(ContentModel.TYPE_CATEGORY);
            }
        }

        String assocVal = "*" + valueStr + "*";

        Map<QName, Serializable> attributes = new HashMap<>();
        attributes.put(ContentModel.PROP_TITLE, assocVal);
        attributes.put(ContentModel.PROP_NAME, assocVal);

        if (targetClass != null) {

            if (targetClass.getName().equals(ContentModel.TYPE_PERSON)) {
                attributes.put(ContentModel.PROP_USERNAME, assocVal);
                attributes.put(ContentModel.PROP_USER_USERNAME, assocVal);
                attributes.put(ContentModel.PROP_FIRSTNAME, assocVal);
                attributes.put(ContentModel.PROP_LASTNAME, assocVal);
            }


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
        }

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

    private void searchStrByText() {

    }
}
