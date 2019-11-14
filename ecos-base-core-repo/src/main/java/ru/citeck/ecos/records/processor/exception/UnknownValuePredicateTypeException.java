package ru.citeck.ecos.records.processor.exception;

import ru.citeck.ecos.predicate.model.ValuePredicate;

public class UnknownValuePredicateTypeException extends RuntimeException{

    public UnknownValuePredicateTypeException(ValuePredicate.Type type) {
        super("Unknown value predicate type: " + type);
    }
}
