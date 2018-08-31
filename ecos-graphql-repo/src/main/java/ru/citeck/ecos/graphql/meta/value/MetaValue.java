package ru.citeck.ecos.graphql.meta.value;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;

import java.util.Optional;

public interface MetaValue {

    @GraphQLField
    String id();

    @GraphQLField
    String str();

    @GraphQLField
    Optional<MetaAttribute> att(@GraphQLName("name") String name);
}
