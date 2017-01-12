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
package ru.citeck.ecos.spring.aop;
import java.util.Map;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Behaviour Filter Interceptor - allows to temporarily disable specified behaviours.
 * It requires active trasaction.
 * 
 * Configuration is done via disabledClasses map, e.g.:
 * setProperty=*
 * *=cm:auditable
 * 
 * "*" in method name means any method.
 * "*" in policy list means any policy (behaviour).
 * 
 * Policies can be comma separated, e.g. cm:auditable,cm:versionable.
 * 
 * @author Sergey Tiunov
 *
 */
public class BehaviourFilterInterceptor implements MethodInterceptor {

    private BehaviourFilter behaviourFilter;
    private NamespacePrefixResolver prefixResolver;
    private Map<String, String> disabledClasses;
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        
        String methodName = invocation.getMethod().getName();
        String classesStr = disabledClasses.get(methodName);
        if(classesStr == null) {
            classesStr = disabledClasses.get("*");
            if(classesStr == null) {
                return invocation.proceed();
            }
        }
        
        String[] classNames = classesStr.split(",");
        try {
            for(String className : classNames) {
                if(className.equals("*")) {
                    behaviourFilter.disableBehaviour();
                } else {
                    behaviourFilter.disableBehaviour(QName.createQName(className, prefixResolver));
                }
            }
            return invocation.proceed();
        } finally {
            for(String className : classNames) {
                if(className.equals("*")) {
                    behaviourFilter.enableBehaviour();
                } else {
                    behaviourFilter.enableBehaviour(QName.createQName(className, prefixResolver));
                }
            }
        }
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

    public void setDisabledClasses(Map<String, String> disabledClasses) {
        this.disabledClasses = disabledClasses;
    }

}
