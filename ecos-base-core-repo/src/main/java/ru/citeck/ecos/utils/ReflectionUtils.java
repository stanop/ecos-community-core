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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {
	
    static List<Method> getMatchingMethods(List<Method> methods, Object[] args) {
        List<Method> matchingMethods = new LinkedList<Method>();
        for(Method method : methods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean matches = true;
            int length1 = parameterTypes.length;
            int length2 = args != null ? args.length : 0;
            if(length1 != length2) continue;
            if(length1 > 0) {
                for(int i = 0; i < length1; i++) {
                    if(args[i] != null && !parameterTypes[i].isInstance(args[i])) {
                        matches = false;
                        break;
                    }
                }
            }
            if(matches) {
                matchingMethods.add(method);
            }
        }
        return matchingMethods;
    }

    public static boolean callSetterIfDeclared(Object that, String methodName, Object value) {
        if(that == null || methodName == null) {
            throw new IllegalArgumentException("Target object and method name are required");
        }
        try {
            Class<?> thatClass = that.getClass();
            Method method = thatClass.getMethod(methodName, value.getClass());
            method.invoke(that, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

	public static Object callGetterIfDeclared(Object that, String methodName, Object returnIfFailed) {
		if(that == null || methodName == null) {
			throw new IllegalArgumentException("Target object and method name are required");
		}
		try {
			Class<?> thatClass = that.getClass();
			Method method = thatClass.getMethod(methodName);
			return method.invoke(that);
		} catch (Exception e) {
			return returnIfFailed;
		}
	}
	
    @SuppressWarnings("rawtypes")
    public static Object queryProperty(Object that, String propertyPath) {
        String[] propertyNames = propertyPath.split("\\.");
        Object result = that;
        for(String propertyName : propertyNames) {
            // try to get property if it is Map:
            if(result instanceof Map) {
                result = ((Map)result).get(propertyName);
                if(result == null) return null;
                continue;
            }
            Class<?> currentClass = result.getClass();
            // try to get property via field:
            try {
                Field field = currentClass.getField(propertyName);
                result = field.get(result);
                if(result == null) return null;
                continue;
            } catch(Exception e) {
                // ignore
            }
            // try to get property via getter:
            try {
                Method getter = result.getClass().getMethod("get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
                result = getter.invoke(result);
                if(result == null) return null;
                continue;
            } catch(Exception e) {
                // ignore
            }
            throw new IllegalStateException("No public field or getter method found for property " + propertyName + " in object of class " + currentClass);
        }
        return result;
    }
    
    public static Class<?> getGenericParameterClass(Class<?> actualClass, int parameterIndex) {
        Type typeArgument = ((ParameterizedType) actualClass.getGenericSuperclass()).getActualTypeArguments()[parameterIndex];
        if(typeArgument instanceof Class) 
            return (Class<?>) typeArgument;
        if(typeArgument instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType) typeArgument).getRawType();
        throw new IllegalArgumentException("Unexpected type argument: " + typeArgument.getClass());
    }

    public static Class<?> getGenericParameterClass(Object object, int parameterIndex) {
        return getGenericParameterClass(object.getClass(), parameterIndex);
    }

}
