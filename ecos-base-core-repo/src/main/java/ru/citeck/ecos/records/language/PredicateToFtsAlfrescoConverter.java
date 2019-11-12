package ru.citeck.ecos.records.language;

import groovy.lang.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.records.processor.exception.PredicateTypeNotSupportedException;
import ru.citeck.ecos.records.processor.PredicateProcessor;
import ru.citeck.ecos.records.processor.impl.ComposedPredicateProcessor;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

@Component
public class PredicateToFtsAlfrescoConverter {

    /*
    *   Using @Lazy init here, because we have circular dependencies
    * */

    @Autowired
    @Lazy
    private PredicateProcessor<ComposedPredicate> composedPredicateProcessor;

    @Autowired
    @Lazy
    private PredicateProcessor<EmptyPredicate> emptyPredicateProcessor;

    @Autowired
    @Lazy
    private PredicateProcessor<NotPredicate> notPredicateProcessor;

    @Autowired
    @Lazy
    private PredicateProcessor<ValuePredicate> valuePredicateProcessor;

    public void processPredicate(Predicate predicate, FTSQuery query) {

        if (predicate instanceof ComposedPredicate) {
            composedPredicateProcessor.process((ComposedPredicate) predicate, query);
        } else if (predicate instanceof NotPredicate) {
            notPredicateProcessor.process((NotPredicate) predicate, query);
        } else if (predicate instanceof ValuePredicate) {
            valuePredicateProcessor.process((ValuePredicate) predicate, query);
        } else if (predicate instanceof EmptyPredicate) {
            emptyPredicateProcessor.process((EmptyPredicate) predicate, query);
        }
        // insert here new predicate type processor
        else {
            throw new PredicateTypeNotSupportedException(predicate);
        }


        // TODO: (HIGH) THINK ABOUT REWORKING CURRENT SOLUTION WITHOUT CALLING 'processPredicate' METHOD FROM DEPENDENCIES
        // TODO: (LOW)  THINK ABOUT FLEXIBLE VARIANT OF HANDLING SWITCHING PROCESSOR
    }
}
