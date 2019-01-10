package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.GqlContext;

import java.util.Collections;
import java.util.List;

/**
 * Metadata value. Used to get attributes by schema
 *
 * @see MetaValueTypeDef
 * @author Pavel Simonov
 */
public interface MetaValue {

    /**
     * Initialize value with context before execute other methods
     */
    default MetaValue init(GqlContext context) {
        return this;
    }

    /**
     * String representation
     */
    String getString();

    /**
     * Value identifier
     */
    default String getId() {
        return null;
    }

    /**
     * Get value attribute
     */
    default List<MetaValue> getAttribute(String attributeName) {
        return Collections.emptyList();
    }

    default boolean hasAttribute(String attributeName) {
        return false;
    }

    default Double getDouble() {
        return Double.parseDouble(getString());
    }

    default Boolean getBool() {
        return null;
    }

    default Object getJson() {
        return getString();
    }

    default MetaValue getAs(String type) {
        return null;
    }
}
