package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.alfnode.CriteriaAlfNodesSearch;
import ru.citeck.ecos.search.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExactCriteriaRecordsDAO extends FilteredRecordsDAO {

    private static final Log logger = LogFactory.getLog(ExactCriteriaRecordsDAO.class);

    private SearchCriteriaParser criteriaParser;

    private List<String> filteredFields = Collections.emptyList();
    private Map<String, PredicateFilter> filters = new HashMap<>();

    public ExactCriteriaRecordsDAO() {

        PredicateFilter exactStrFilter = new PredicateFilter("str", (value, array) -> {
            String arrayValue = "";
            if (array != null && array.size() > 0) {
                JsonNode strNode = array.get(0).get("str");
                arrayValue = strNode.isTextual() ? strNode.asText() : "";
            }
            if (StringUtils.isBlank(value)) {
                return arrayValue.length() == 0;
            } else {
                return Objects.equals(value, arrayValue);
            }
        });

        PredicateFilter emptyStrFilter = new PredicateFilter("str",
                (value, array) -> exactStrFilter.filter.apply("", array));

        filters.put(SearchPredicate.STRING_EMPTY.getValue(), emptyStrFilter);
        filters.put(SearchPredicate.DATE_EMPTY.getValue(), emptyStrFilter);
        filters.put(SearchPredicate.STRING_EQUALS.getValue(), exactStrFilter);
    }

    @Override
    protected Function<List<RecordRef>, List<RecordRef>> getFilter(RecordsQuery query) {

        if (!CriteriaAlfNodesSearch.LANGUAGE.equals(query.getLanguage())) {
            return list -> list;
        }

        SearchCriteria criteria = criteriaParser.parse(query.getQuery());

        StringBuilder metaQuery = new StringBuilder("id");
        List<CriterionFilter> criterionFilters = new ArrayList<>();

        AtomicInteger attCounter = new AtomicInteger();

        criteria.getTriplets().forEach(t -> {

            if (filteredFields.contains(t.getField())) {

                PredicateFilter filter = filters.get(t.getPredicate());
                if (filter == null) {

                    logger.warn("Predicate filter for '" + t.getPredicate() +
                                "' not found. This field will be ignored");
                } else {

                    String fieldKey = "a" + attCounter.getAndIncrement();

                    CriterionFilter criterionFilter = new CriterionFilter();
                    criterionFilter.fieldKey = fieldKey;
                    criterionFilter.fieldValue = t.getValue();
                    criterionFilter.predicateFilter = filter;

                    criterionFilters.add(criterionFilter);

                    metaQuery.append("\n")
                             .append(fieldKey)
                             .append(":att(name:\"")
                             .append(t.getField())
                             .append("\"){val{")
                             .append(filter.metaSchema)
                             .append("}}");
                }
            }
        });

        if (criterionFilters.isEmpty()) {
            return list -> list;
        } else {
            return list -> {
                List<ObjectNode> meta = recordsService.getMeta(list, metaQuery.toString());
                return meta.stream()
                           .filter(m -> criterionFilters.stream().allMatch(f -> f.apply(m)))
                           .map(m -> new RecordRef(m.get("id").asText()))
                           .collect(Collectors.toList());
            };
        }
    }

    @Autowired
    public void setCriteriaParser(SearchCriteriaParser criteriaParser) {
        this.criteriaParser = criteriaParser;
    }

    public void setFilteredFields(List<String> filteredFields) {
        this.filteredFields = filteredFields;
    }

    private class CriterionFilter {

        String fieldKey;
        String fieldValue;
        PredicateFilter predicateFilter;

        boolean apply(ObjectNode nodeData) {
            JsonNode attNode = nodeData.get(fieldKey);
            if (attNode instanceof ObjectNode) {
                attNode = attNode.get("val");
            }
            ArrayNode arrayNode = attNode instanceof ArrayNode ? (ArrayNode) attNode : null;
            return predicateFilter.filter.apply(fieldValue, arrayNode);
        }
    }

    private class PredicateFilter {

        String metaSchema;
        BiFunction<String, ArrayNode, Boolean> filter;

        PredicateFilter(String metaSchema,
                        BiFunction<String, ArrayNode, Boolean> filter) {

            this.metaSchema = metaSchema;
            this.filter = filter;
        }
    }
}
