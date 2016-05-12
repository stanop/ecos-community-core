/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.utils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class SimpleMethodInterceptor implements MethodInterceptor {

    private Map<String, List<Method>> methodMap;
    
    public SimpleMethodInterceptor() {
        methodMap = new TreeMap<String, List<Method>>();
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        
        Method matchingMethod = this.getMatchingMethod(method, args);
        if(matchingMethod != null) {
            return matchingMethod.invoke(this, args);
        } else {
            return invocation.proceed();
        }
    }
    
    protected Method getMatchingMethod(Method method, Object[] args) {
        List<Method> overloadedMethods = getOverloadedMethods(method.getName());
        if(overloadedMethods.size() == 0) return null;
        List<Method> matchingMethods = ReflectionUtils.getMatchingMethods(overloadedMethods, args);
        if(matchingMethods.size() == 0) return null;
        return matchingMethods.get(0);
    }

    protected List<Method> getOverloadedMethods(String methodName) {
        List<Method> overloadedMethods = methodMap.get(methodName);
        if(overloadedMethods == null) {
            overloadedMethods = new LinkedList<Method>();
            methodMap.put(methodName, overloadedMethods);
            
            Method[] methods = this.getClass().getMethods();
            for(Method method : methods) {
                if(method.getName().equals(methodName)) {
                    overloadedMethods.add(method);
                }
            }
        }
        return overloadedMethods;
    }
}