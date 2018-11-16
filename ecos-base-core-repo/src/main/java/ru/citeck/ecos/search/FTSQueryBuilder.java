package ru.citeck.ecos.search;

import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FTSQueryBuilder implements SearchQueryBuilder {

    private static final Log logger = LogFactory.getLog(FTSQueryBuilder.class);

    private static final String WILD = "*";
    private static final String NOT = " NOT ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";

    private FtsAlfrescoQueryMigration legacyBuilder;

    @Override
    public String buildQuery(SearchCriteria criteria) {

        boolean legacyMode = criteria.getTriplets()
                                     .stream()
                                     .noneMatch(c -> c.getPredicate().startsWith("join-by-"));

        if (legacyMode) {
            return legacyBuilder.buildQuery(criteria);
        }

        return null;
    }

    private Optional<Term> buildSearchTerm(CriteriaTriplet triplet) {

        triplet = legacyBuilder.convertAssocTriplet(triplet);
        SearchPredicate criterion = SearchPredicate.forName(triplet.getPredicate());

        String value = triplet.getValue();
        String field = buildField(triplet.getField());

        switch (criterion) {
            case STRING_CONTAINS:
                //buildEqualsTerm(field, WILD + value + WILD);
                break;
            case QUERY_OR:
                //buildQueryOr(triplet.getField(), value);
                break;
            case STRING_NOT_EQUALS:
            case NUMBER_NOT_EQUALS:
            case DATE_NOT_EQUALS:
            case TYPE_NOT_EQUALS:
            case ASPECT_NOT_EQUALS:
                //query.append(NOT);
            case STRING_EQUALS:
            case NUMBER_EQUALS:
            case DATE_EQUALS:
                //buildEqualsTermWithRoundBracket(field, value);
                break;
            case TYPE_EQUALS:
            case ASPECT_EQUALS:
            case PARENT_EQUALS:
            case PATH_EQUALS:
            case LIST_EQUALS:
            case LIST_NOT_EQUALS:
                //buildEqualsTerm(field, value);
                break;
            case STRING_STARTS_WITH:
                //buildEqualsTerm(field, value + WILD);
                break;
            case STRING_ENDS_WITH:
                //buildEqualsTerm(field, WILD + value);
                break;
            case DATE_NOT_EMPTY:
            case BOOLEAN_NOT_EMPTY:
            case NODEREF_NOT_EMPTY:
            case ASSOC_NOT_EMPTY:
            case FLOAT_NOT_EMPTY:
            case INT_NOT_EMPTY:
            case STRING_NOT_EMPTY:
                //buildEmptyCheckTerm(field, false);
                break;
            case DATE_EMPTY:
            case BOOLEAN_EMPTY:
            case NODEREF_EMPTY:
            case ASSOC_EMPTY:
                //buildNullCheckTerm(field, true);
                break;
            case STRING_EMPTY:
                //buildEmptyCheckTerm(field, true);
                break;
            case NUMBER_LESS_THAN:
                //buildLessThanTerm(field, value, false);
                break;
            case NUMBER_GREATER_THAN:
                //buildGreaterThanTerm(field, value, false);
                break;
            case NUMBER_LESS_OR_EQUAL:
            case DATE_LESS_OR_EQUAL:
            case DATE_LESS_THAN:
                //buildLessThanTerm(field, value, true);
                break;
            case NUMBER_GREATER_OR_EQUAL:
            case DATE_GREATER_OR_EQUAL:
                //buildGreaterThanTerm(field, value, true);
                break;
            case BOOLEAN_TRUE:
                //buildEqualsTerm(field, "true");
                break;
            case BOOLEAN_FALSE:
                //buildEqualsTerm(field, "false");
                break;
            case ANY:
                //buildEqualsTerm(field, WILD);
                break;
            case NODEREF_NOT_CONTAINS:
            case ASSOC_NOT_CONTAINS:
            case QNAME_NOT_CONTAINS:
                //query.append(NOT);
            case NODEREF_CONTAINS:
            case ASSOC_CONTAINS:
            case QNAME_CONTAINS:
                //buildListContainsTerm(field, value);
                break;
            case PATH_CHILD:
                //buildEqualsTerm(field, value + "/*");
                break;
            case PATH_DESCENDANT:
                //buildEqualsTerm(field, value + "//*");
                break;
            case ID_EQUALS:
                break;
        }

        return Optional.empty();
    }

    private String buildField(String field) {
        try {
            return FieldType.forName(field).toString();
        } catch (IllegalArgumentException e) {
            return "@" + LuceneQuery.escapeField(field);
        }
    }

    @Override
    public boolean supports(String language) {
        return SearchService.LANGUAGE_FTS_ALFRESCO.equals(language);
    }

    @Autowired
    public void setLegacyBuilder(FtsAlfrescoQueryMigration legacyBuilder) {
        this.legacyBuilder = legacyBuilder;
    }

    private class QueryBuilder {

    }

    interface Term {
        void append(StringBuilder sb);
    }

    static class FixedTerm implements Term {

        final String value;

        public FixedTerm(String value) {
            this.value = value;
        }

        @Override
        public void append(StringBuilder sb) {
            sb.append(value);
        }
    }

    static class TermGroup implements Term {

        private final String joinDelim;
        private final List<Term> terms = new ArrayList<>();
        private final boolean withBrackets;

        private TermGroup childGroup = null;

        TermGroup(String joinDelim) {
            this(joinDelim, true);
        }

        TermGroup(String joinDelim, boolean withBrackets) {
            this.joinDelim = joinDelim;
            this.withBrackets = withBrackets;
        }

        void startGroup(String joinDelim) {
            if (childGroup != null) {
                childGroup.startGroup(joinDelim);
            } else {
                childGroup = new TermGroup(joinDelim);
            }
        }

        void stopGroup() {
            if (childGroup != null) {
                if (childGroup.childGroup != null) {
                    childGroup.stopGroup();
                }
                terms.add(childGroup);
                childGroup = null;
            }
        }

        void add(Term term) {
            terms.add(term);
        }

        @Override
        public void append(StringBuilder sb) {
            if (terms.size() == 0) {
                return;
            }
            if (withBrackets) {
                sb.append('(');
            }
            terms.get(0).append(sb);
            for (int i = 1; i < terms.size(); i++) {
                sb.append(joinDelim);
                terms.get(i).append(sb);
            }
            if (withBrackets) {
                sb.append(')');
            }
        }
    }
}


