package ru.citeck.ecos.graphql.meta.converter;

public @interface MetaAttr {

    String name() default "";

    Class<? extends MetaConverter<?>> converter() default NotValidConverter.class;

}
