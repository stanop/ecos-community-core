package ru.citeck.ecos.graphql.meta.value;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLID;
import graphql.annotations.annotationTypes.GraphQLName;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface MetaValue {

    @GraphQLField
    @GraphQLID
    String id();

    @GraphQLField
    String str();

    @GraphQLField
    default MetaValue as(@GraphQLName("type") String type) {
        return this;
    }

    @GraphQLField
    default Optional<MetaValue> as(@GraphQLName("type") String type) {
        return Optional.empty();
    }

    @GraphQLField
    default String str() {
        return id();
    }

    @GraphQLField
    default Optional<MetaAttribute> att(@GraphQLName("name") String name) {
        return Optional.empty();
    }

    @GraphQLField
    default List<MetaAttribute> atts(@GraphQLName("filter") String filter) {
        return Collections.emptyList();
    }
}
