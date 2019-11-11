package ru.citeck.ecos.records.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.model.NotPredicate;
import ru.citeck.ecos.records.language.PredicateToFtsAlfrescoConverter;
import ru.citeck.ecos.records.processor.PredicateProcessor;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

@Component
public class NotPredicateProcessor implements PredicateProcessor<NotPredicate> {

    private PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter;

    @Autowired
    public void setPredicateToFtsAlfrescoConverter(PredicateToFtsAlfrescoConverter predicateToFtsAlfrescoConverter) {
        this.predicateToFtsAlfrescoConverter = predicateToFtsAlfrescoConverter;
    }

    @Override
    public void process(NotPredicate predicate, FTSQuery query) {
        query.not();
        predicateToFtsAlfrescoConverter.processPredicate(predicate.getPredicate(), query);
    }
}
