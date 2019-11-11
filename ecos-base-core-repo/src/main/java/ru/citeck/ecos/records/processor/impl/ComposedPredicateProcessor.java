package ru.citeck.ecos.records.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.model.AndPredicate;
import ru.citeck.ecos.predicate.model.ComposedPredicate;
import ru.citeck.ecos.predicate.model.Predicate;
import ru.citeck.ecos.records.language.PredicateToFtsAlfrescoConverter;
import ru.citeck.ecos.records.processor.PredicateProcessor;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.util.List;

@Component
public class ComposedPredicateProcessor implements PredicateProcessor<ComposedPredicate> {

    private PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter;

    @Autowired
    public void setPredicateToFtsAlfrescoConverter(PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter) {
        this.predicateToFtsAlfrescoConverter = predicateToFtsAlfrescoConverter;
    }

    @Override
    public void process(ComposedPredicate predicate, FTSQuery query) {

        query.open();

        List<Predicate> predicates = predicate.getPredicates();
        boolean isJoinByAnd = predicate instanceof AndPredicate;

        for (int i = 0; i < predicates.size(); i++) {
            if (i > 0) {
                if (isJoinByAnd) {
                    query.and();
                } else {
                    query.or();
                }
            }

            predicateToFtsAlfrescoConverter.processPredicate(predicates.get(i), query);
        }

        query.close();
    }
}
