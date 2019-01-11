package ru.citeck.ecos.graphql.meta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaExplicitValue;
import ru.citeck.ecos.graphql.meta.value.MetaValue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetaUtils {

    private static final Log logger = LogFactory.getLog(MetaUtils.class);

    public static List<MetaValue> toMetaValues(Object value, GqlContext context) {

        List<MetaValue> values;

        if (value instanceof Collection) {
            values = ((Collection<?>) value).stream()
                    .map(MetaExplicitValue::new)
                    .collect(Collectors.toList());
        } else {
            values = Collections.singletonList(new MetaExplicitValue(value));
        }

        values.forEach(v -> v.init(context));

        return values;
    }

    public static List<MetaValue> getReflectionValue(Object scope, String methodName) {

        if (scope == null || methodName == null) {
            return Collections.emptyList();
        }
        Class<?> clazz = scope.getClass();
        try {
            Method method = clazz.getMethod(methodName);
            MetaValue value = new MetaExplicitValue(method.invoke(scope));
            return Collections.singletonList(value);
        } catch (NoSuchMethodException e) {
            logger.error("Method " + methodName + " not found in " + clazz.getName(), e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Error while method invocation. Method: " + methodName + " Class: " + clazz, e);
        }
        return Collections.emptyList();
    }
}
