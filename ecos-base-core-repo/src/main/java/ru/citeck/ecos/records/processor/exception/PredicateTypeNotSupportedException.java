package ru.citeck.ecos.records.processor.exception;

import ru.citeck.ecos.predicate.model.Predicate;

public class PredicateTypeNotSupportedException extends RuntimeException {

    public PredicateTypeNotSupportedException(Predicate predicate) {
        super("Predicate type not supported: " + predicate);
    }
}
