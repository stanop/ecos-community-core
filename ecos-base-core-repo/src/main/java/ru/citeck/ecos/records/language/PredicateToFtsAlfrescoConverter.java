package ru.citeck.ecos.records.language;

import groovy.lang.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.records.processor.PredicateProcessor;
import ru.citeck.ecos.records.processor.exception.PredicateTypeNotSupportedException;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

@Component
public class PredicateToFtsAlfrescoConverter {

    private PredicateProcessor<ComposedPredicate> composedPredicateProcessor;
    private PredicateProcessor<EmptyPredicate> emptyPredicateProcessor;
    private PredicateProcessor<NotPredicate> notPredicateProcessor;
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

    public PredicateProcessor<ComposedPredicate> getComposedPredicateProcessor() {
        return composedPredicateProcessor;
    }

    @Autowired
    public void setComposedPredicateProcessor(PredicateProcessor<ComposedPredicate> composedPredicateProcessor) {
        this.composedPredicateProcessor = composedPredicateProcessor;
    }

    public PredicateProcessor<EmptyPredicate> getEmptyPredicateProcessor() {
        return emptyPredicateProcessor;
    }

    @Autowired
    public void setEmptyPredicateProcessor(PredicateProcessor<EmptyPredicate> emptyPredicateProcessor) {
        this.emptyPredicateProcessor = emptyPredicateProcessor;
    }

    public PredicateProcessor<NotPredicate> getNotPredicateProcessor() {
        return notPredicateProcessor;
    }

    @Autowired
    public void setNotPredicateProcessor(PredicateProcessor<NotPredicate> notPredicateProcessor) {
        this.notPredicateProcessor = notPredicateProcessor;
    }

    public PredicateProcessor<ValuePredicate> getValuePredicateProcessor() {
        return valuePredicateProcessor;
    }

    @Autowired
    public void setValuePredicateProcessor(PredicateProcessor<ValuePredicate> valuePredicateProcessor) {
        this.valuePredicateProcessor = valuePredicateProcessor;
    }
}
