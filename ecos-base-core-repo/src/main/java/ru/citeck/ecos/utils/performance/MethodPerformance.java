package ru.citeck.ecos.utils.performance;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

public class MethodPerformance extends Performance {

    private Method method;
    private Object[] args;

    public MethodPerformance(Object instance, Method method, Object[] args) {
        super(instance);
        this.method = method;
        this.args = args;
    }

    @Override
    public String toString() {
        String methodName = method != null ? method.getName() : "null";
        methodName = methodName != null ? methodName : "null";
        return "." + methodName + "(" + StringUtils.join(args, ", ") + ")";
    }
}
