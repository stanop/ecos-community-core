package ru.citeck.ecos.icase;

import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.records2.predicate.model.Predicates;
import ru.citeck.ecos.records2.predicate.model.ValuePredicate;

import java.util.Date;
import java.util.Objects;

public class CasePredicateUtils {

    public static Predicate getSimplePredicate(String propertyName, Operation operation, Object value) {
        switch (operation) {
            case EQUALS:
                return Predicates.eq(propertyName, value);
            case CONTAINS:
                String containsValue = Objects.toString(value);
                return Predicates.contains(propertyName, containsValue);
            case BEGINS:
                String beginsValue = Objects.toString(value);
                return new ValuePredicate(propertyName, ValuePredicate.Type.LIKE, beginsValue + "%");
            case ENDS:
                String endsValue = Objects.toString(value);
                return new ValuePredicate(propertyName, ValuePredicate.Type.LIKE, "%" + endsValue);
            case GREATER_THAN:
                if (isDate(value)) {
                    return Predicates.gt(propertyName, (Date) value);
                } else if (isNumber(value)) {
                    return Predicates.gt(propertyName, ((Number) value).doubleValue());
                }
                throw new IllegalArgumentException("Value " + value + "  is not supportable by GREATER_THAT operator");
            case GREATER_THAN_EQUAL:
                if (isDate(value)) {
                    return Predicates.ge(propertyName, (Date) value);
                } else if (isNumber(value)) {
                    return Predicates.ge(propertyName, ((Number) value).doubleValue());
                }
                throw new IllegalArgumentException("Value " + value + "  is not supportable by GREATER_THAT_EQUAL operator");
            case LESS_THAN:
                if (isDate(value)) {
                    return Predicates.lt(propertyName, (Date) value);
                } else if (isNumber(value)) {
                    return Predicates.lt(propertyName, ((Number) value).doubleValue());
                }
                throw new IllegalArgumentException("Value " + value + "  is not supportable by LESS_THAN operator");
            case LESS_THAN_EQUAL:
                if (isDate(value)) {
                    return Predicates.le(propertyName, (Date) value);
                } else if (isNumber(value)) {
                    return Predicates.le(propertyName, ((Number) value).doubleValue());
                }
                throw new IllegalArgumentException("Value " + value + "  is not supportable by LESS_THAN_EQUAL operator");
        }

        throw new IllegalArgumentException(String.format("Error while creating predicate %s, %s, %s",
                propertyName, operation, value));
    }

    private static boolean isDate(Object value) {
        return value instanceof Date;
    }

    private static boolean isNumber(Object value) {
        return value instanceof Number;
    }

    public enum Operation {
        EQUALS,
        CONTAINS,
        BEGINS,
        ENDS,
        GREATER_THAN,
        GREATER_THAN_EQUAL,
        LESS_THAN,
        LESS_THAN_EQUAL
    }

}
