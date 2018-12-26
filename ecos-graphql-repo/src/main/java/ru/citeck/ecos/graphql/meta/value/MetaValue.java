package ru.citeck.ecos.graphql.meta.value;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLID;
import graphql.annotations.annotationTypes.GraphQLName;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

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
    Optional<MetaAttribute> att(@GraphQLName("name") String name);

    @GraphQLField
    List<MetaAttribute> atts(@GraphQLName("filter") String filter);
}
