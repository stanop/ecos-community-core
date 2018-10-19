package ru.citeck.ecos.graphql.meta.attribute;

import graphql.annotations.annotationTypes.GraphQLField;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.util.List;

public interface MetaAttribute {

    @GraphQLField
    String name();

    @GraphQLField
    List<MetaValue> val();
}
