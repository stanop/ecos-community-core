package ru.citeck.ecos.records.language;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.querylang.QueryLangConverter;
import ru.citeck.ecos.querylang.QueryLangService;
import ru.citeck.ecos.records.source.alf.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.search.FTSQueryBuilder;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class CriteriaToPredicateConverter implements QueryLangConverter {

    private Pattern FIX_VALUE_PATTERN = Pattern.compile("([=@])?([^\"]+):\"?(.*?)\"?$");

    private FTSQueryBuilder ftsQueryBuilder;
    private SearchCriteriaParser criteriaParser;
    private PredicateService predicateService;

    @Autowired
    public CriteriaToPredicateConverter(QueryLangService queryLangService,
                                        FTSQueryBuilder ftsQueryBuilder,
                                        SearchCriteriaParser criteriaParser,
                                        PredicateService predicateService) {

        this.criteriaParser = criteriaParser;
        this.ftsQueryBuilder = ftsQueryBuilder;
        this.predicateService = predicateService;

        queryLangService.register(this,
                CriteriaAlfNodesSearch.LANGUAGE,
                PredicateService.LANGUAGE_PREDICATE);
    }

    private Predicate convertCriteriaTerm(FTSQueryBuilder.Term term) {

        if (term instanceof FTSQueryBuilder.TermGroup) {

            ComposedPredicate composedPred;

            FTSQueryBuilder.TermGroup group = (FTSQueryBuilder.TermGroup) term;

            switch (group.getJoinBy()) {
                case OR:
                    composedPred = new OrPredicate();
                    break;
                case AND:
                    composedPred = new AndPredicate();
                    break;
                default:
                    throw new IllegalStateException("Unknown operator: " + group.getJoinBy());
            }

            for (FTSQueryBuilder.Term t : group.getTerms()) {
                composedPred.addPredicate(convertCriteriaTerm(t));
            }

            return composedPred;

        } else if (term instanceof FTSQueryBuilder.FixedTerm) {

            FTSQueryBuilder.FixedTerm fixedTerm = (FTSQueryBuilder.FixedTerm) term;
            String termValue = fixedTerm.getValue().trim();

            if (termValue.startsWith("(")) {
                termValue = termValue.substring(1, termValue.length() - 1);
            }

            if (termValue.contains(" OR ")) {
                FTSQueryBuilder.TermGroup group = new FTSQueryBuilder.TermGroup(FTSQueryBuilder.JoinOperator.OR);
                String[] terms = termValue.split(" OR ");
                group.setTerms(Arrays.stream(terms)
                                     .map(FTSQueryBuilder.FixedTerm::new)
                                     .collect(Collectors.toList()));
                return convertCriteriaTerm(group);
            }
            if (termValue.contains(" AND ")) {
                FTSQueryBuilder.TermGroup group = new FTSQueryBuilder.TermGroup(FTSQueryBuilder.JoinOperator.AND);
                String[] terms = termValue.split(" AND ");
                group.setTerms(Arrays.stream(terms)
                                     .map(FTSQueryBuilder.FixedTerm::new)
                                     .collect(Collectors.toList()));
                return convertCriteriaTerm(group);
            }

            String fixedTermValue = fixedTerm.getValue();
            boolean isInverse = fixedTerm.isInverse();
            if (fixedTermValue.startsWith("NOT ")) {
                isInverse = true;
                fixedTermValue = fixedTermValue.substring("NOT ".length());
            }

            Matcher matcher = FIX_VALUE_PATTERN.matcher(fixedTermValue);

            if (!matcher.matches()) {
                throw new RuntimeException("Unknown fixed term: " + fixedTerm.getValue());
            }

            String eqOrLike = matcher.group(1);

            String field = matcher.group(2);
            String value = matcher.group(3);

            ValuePredicate pred = new ValuePredicate();
            pred.setAttribute(field.replace("\\:", ":"));

            if ("@".equals(eqOrLike)) {
                pred.setType(ValuePredicate.Type.LIKE);
                pred.setValue(value.replaceAll("\\*", "%"));
            } else {
                pred.setType(ValuePredicate.Type.EQ);
                pred.setValue(value);
            }

            return isInverse ? new NotPredicate(pred) : pred;
        }

        throw new RuntimeException("Unknown term: " + term);
    }

    @Override
    public JsonNode convert(JsonNode query) {
        SearchCriteria criteria = criteriaParser.parse(query);
        return predicateService.writeJson(convertCriteriaTerm(ftsQueryBuilder.getTermGroup(criteria)));
    }
}
