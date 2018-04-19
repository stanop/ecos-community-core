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
package ru.citeck.ecos.behavior;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.policy.BaseBehaviour;
import org.alfresco.repo.policy.PolicyException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.extensions.surf.util.ParameterCheck;
import ru.citeck.ecos.utils.performance.MethodPerformance;

/**
 * Parameterized Java based Behaviour.
 * 
 * Java based method-delegating behaviour.
 * Acts like default JavaBehaviour, but also passes additional parameter to method as a last argument.
 * 
 * @author Sergey Tiunov
 *
 */
@AlfrescoPublicApi
public class ParameterizedJavaBehaviour<P> extends BaseBehaviour
{
    // The object instance holding the method
    private Object instance;
    
    // The method name
    private String method;
    
    // execution parameter
    private P parameter;

    /**
     * Construct.
     * 
     * @param instance  the object instance holding the method
     * @param method  the method name
     */
    private ParameterizedJavaBehaviour(Object instance, String method, NotificationFrequency frequency, P parameter)
    {
        super(frequency);
        ParameterCheck.mandatory("Instance", instance);
        ParameterCheck.mandatory("Method", method);
        this.method = method;
        this.instance = instance;
        this.parameter = parameter;
    }
    
    public static <P> ParameterizedJavaBehaviour<P> newInstance(Object instance, String method, P parameter) {
        return new ParameterizedJavaBehaviour<P>(instance, method, NotificationFrequency.EVERY_EVENT, parameter);
    }

    public static <P> ParameterizedJavaBehaviour<P> newInstance(Object instance, String method, NotificationFrequency frequency, P parameter) {
        return new ParameterizedJavaBehaviour<P>(instance, method, frequency, parameter);
    }
    

    @Override
    public String toString()
    {
        return "Java method[class=" + instance.getClass().getName() + ", method=" + method + "]";
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <T> T getInterface(Class<T> policy) 
    {
        ParameterCheck.mandatory("Policy class", policy);
        Object proxy = proxies.get(policy);
        if (proxy == null)
        {
            InvocationHandler handler = getInvocationHandler(instance, method, policy);
            proxy = Proxy.newProxyInstance(policy.getClassLoader(), new Class[]{policy}, handler);
            proxies.put(policy, proxy);
        }
        return (T)proxy;
    }

    /**
     * Gets the Invocation Handler.
     * 
     * @param <T>  the policy interface class
     * @param instance  the object instance
     * @param method  the method name
     * @param policyIF  the policy interface class  
     * @return  the invocation handler
     */
    <T> InvocationHandler getInvocationHandler(Object instance, String method, Class<T> policyIF)
    {
        Method[] policyIFMethods = policyIF.getMethods();
        if (policyIFMethods.length != 1)
        {
            throw new PolicyException("Policy interface " + policyIF.getCanonicalName() + " must have only one method");
        }

        try
        {
            Class<?>[] parameterTypes = policyIFMethods[0].getParameterTypes();
            Class<?>[] extendedParameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length + 1);
            extendedParameterTypes[extendedParameterTypes.length-1] = parameter.getClass();
            
            Class<?> instanceClass = instance.getClass();
            Method delegateMethod = instanceClass.getMethod(method, extendedParameterTypes);
            return new JavaMethodInvocationHandler<P>(this, delegateMethod);
        }
        catch (NoSuchMethodException e)
        {
            throw new PolicyException("Method " + method + " not found or accessible on " + instance.getClass(), e);
        }
    }    
    
    /**
     * Java Method Invocation Handler
     * 
     * @author David Caruana
     */
    private static class JavaMethodInvocationHandler<P> implements InvocationHandler
    {
        private ParameterizedJavaBehaviour<P> behaviour;
        private Method delegateMethod;
        
        /**
         * Construct.
         * 
         * @param instance  the object instance holding the method
         * @param delegateMethod  the method to invoke
         */
        private JavaMethodInvocationHandler(ParameterizedJavaBehaviour<P> behaviour, Method delegateMethod)
        {
            this.behaviour = behaviour;
            this.delegateMethod = delegateMethod;
        }

        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            MethodPerformance perf = new MethodPerformance(behaviour.instance, delegateMethod, args);

            // Handle Object level methods
            if (method.getName().equals("toString"))
            {
                return toString();
            }
            else if (method.getName().equals("hashCode"))
            {
                return hashCode();
            }
            else if (method.getName().equals("equals"))
            {
                if (Proxy.isProxyClass(args[0].getClass()))
                {
                    return equals(Proxy.getInvocationHandler(args[0]));
                }
                return false;
            }
            
            // Delegate to designated method pointer
            if (behaviour.isEnabled())
            {
                try
                {
                    behaviour.disable();
                    Object[] extendedArgs = Arrays.copyOf(args, args.length + 1);
                    extendedArgs[extendedArgs.length - 1] = behaviour.parameter;
                    return delegateMethod.invoke(behaviour.instance, extendedArgs);
                }
                catch (InvocationTargetException e)
                {
                    throw e.getTargetException();
                }
                finally
                {
                    behaviour.enable();
                    perf.stop();
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            else if (obj == null || !(obj instanceof JavaMethodInvocationHandler))
            {
                return false;
            }
            JavaMethodInvocationHandler<?> that = (JavaMethodInvocationHandler<?>)obj;
            return new EqualsBuilder()
                    .append(this.behaviour.instance, that.behaviour.instance)
                    .append(this.delegateMethod, that.delegateMethod)
                    .append(this.behaviour.parameter, that.behaviour.parameter)
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(behaviour.instance)
                    .append(delegateMethod)
                    .append(behaviour.parameter)
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return "ParameterizedJavaBehaviour[instance=" + behaviour.instance.hashCode() + ", method=" + delegateMethod.toString() + ", parameter=" + behaviour.parameter + "]";
        }
    }

}
