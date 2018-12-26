package ru.citeck.ecos.graphql.meta.annotation;

import ru.citeck.ecos.graphql.meta.converter.MetaConverter;
import ru.citeck.ecos.graphql.meta.converter.NotValidConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaAtt {

    String value() default "";

    Class<? extends MetaConverter<?>> converter() default NotValidConverter.class;

}
