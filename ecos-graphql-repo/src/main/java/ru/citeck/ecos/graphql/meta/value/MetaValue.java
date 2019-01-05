package ru.citeck.ecos.graphql.meta.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
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

    default List<MetaValue> getAttribute(String attributeName, GqlContext context) {
        return Collections.emptyList();
    }

    default JsonNode getJson(GqlContext context) {
        return TextNode.valueOf(getString(context));
    }

    default Double getDouble(GqlContext context) {
        return Double.parseDouble(getString(context));
    }

    default MetaValue getAs(String type, GqlContext context) {
        return null;
    }

    /*MetaEdge edge(String name, GqlContext context);*/
}
