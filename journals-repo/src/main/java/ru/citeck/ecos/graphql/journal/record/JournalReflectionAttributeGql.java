package ru.citeck.ecos.graphql.journal.record;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.AlfNodeAttributeValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class JournalReflectionAttributeGql implements JournalAttributeGql {

    private static final Log logger = LogFactory.getLog(JournalReflectionAttributeGql.class);

    private Object object;
    private String method;

    private GqlContext context;

    public JournalReflectionAttributeGql(Object object, String method, GqlContext context) {
        this.object = object;
        this.method = method;
        this.context = context;
    }

    @Override
    public String name() {
        return method;
    }

    @Override
    public List<JournalAttributeValueGql> val() {
        if (object == null || method == null) {
            return Collections.emptyList();
        }
        Class<?> clazz = object.getClass();
        try {
            Method method = clazz.getMethod(this.method);
            JournalAttributeValueGql value = new AlfNodeAttributeValue(method.invoke(object), context);
            return Collections.singletonList(value);
        } catch (NoSuchMethodException e) {
            logger.error("Method " + method + " not found in " + clazz.getName(), e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Error while method invocation. Method: " + method + " Class: " + clazz, e);
        }
        return Collections.emptyList();
    }
}
