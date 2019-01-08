package ru.citeck.ecos.graphql.meta.value;

import ru.citeck.ecos.graphql.GqlContext;

import java.util.Collections;
import java.util.List;

public interface MetaValue {

    /**
     * String representation
     */
    String getString(GqlContext context);

    /**
     * Value identifier
     */
    default String getId(GqlContext context) {
        return null;
    }

    /**
     * Get value attribute
     */
    default List<MetaValue> getAttribute(String attributeName, GqlContext context) {
        return Collections.emptyList();
    }

    default Double getDouble(GqlContext context) {
        return Double.parseDouble(getString(context));
    }

    default MetaValue getAs(String type, GqlContext context) {
        return null;
    }
}
