package ru.citeck.ecos.graphql.meta.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaAtt {

    String name() default "";

    Class<? extends MetaConverter<?>> converter() default NotValidConverter.class;

}
