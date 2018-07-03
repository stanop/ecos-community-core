package ru.citeck.ecos.graphql.journal.record.attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeGql;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeValueGql;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JournalReflectionAttributeGql implements JournalAttributeGql {

    private static final Log logger = LogFactory.getLog(JournalReflectionAttributeGql.class);

    private Object object;
    private String method;

    public JournalReflectionAttributeGql(Object object, String method) {
        if (object instanceof Optional) {
            this.object = ((Optional<?>) object).orElse(null);
        } else {
            this.object = object;
        }
        this.method = method;
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
            JournalAttributeValueGql value = new JournalAttributeExplicitValue(method.invoke(object));
            return Collections.singletonList(value);
        } catch (NoSuchMethodException e) {
            logger.error("Method " + method + " not found in " + clazz.getName(), e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Error while method invocation. Method: " + method + " Class: " + clazz, e);
        }
        return Collections.emptyList();
    }
}
