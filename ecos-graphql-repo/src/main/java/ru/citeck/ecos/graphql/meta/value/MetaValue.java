package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.GqlContext;

import java.util.Collections;
import java.util.List;

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

    default Double getDouble() {
        return Double.parseDouble(getString());
    }

    default MetaValue getAs(String type) {
        return null;
    }
}
