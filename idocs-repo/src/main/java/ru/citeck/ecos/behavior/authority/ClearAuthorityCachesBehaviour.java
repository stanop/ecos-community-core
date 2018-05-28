/*
 * Copyright (C) 2008-2018 Citeck LLC.
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
package ru.citeck.ecos.behavior.authority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class ClearAuthorityCachesBehaviour implements OnCreateChildAssociationPolicy,
        OnDeleteChildAssociationPolicy, ApplicationContextAware {
    private static Log logger = LogFactory.getLog(ClearAuthorityCachesBehaviour.class);

    private PolicyComponent policyComponent;
    private Map<String, String> caches;
    private ApplicationContext applicationContext;

    public void init() {
        this.policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER,
                ContentModel.ASSOC_MEMBER,
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindAssociationBehaviour(
                OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER,
                ContentModel.ASSOC_MEMBER,
                new JavaBehaviour(this, "onDeleteChildAssociation", NotificationFrequency.EVERY_EVENT)
        );
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }
    
    public void setCaches(Map<String, String> caches) {
        this.caches = caches;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef nodeAssocRef) {
        clearCaches();
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef nodeAssocRef,
            boolean isNewNode) {
        clearCaches();
    }
    
    private void clearCaches() {
        for(String cacheName : caches.keySet()) {
            String methodName = null;
            try {
                Object cache = applicationContext.getBean(cacheName);
                methodName = caches.get(cacheName);
                Method method = cache.getClass().getMethod(methodName);
                method.invoke(cache);
            } catch (NoSuchBeanDefinitionException e) {
                logger.debug("Could not find cache to clear: " + cacheName, e);
            } catch (BeansException e) {
                logger.error("Could not obtain cache bean: " + cacheName, e);
            } catch (NoSuchMethodException e) {
                logger.error("No such method: " + cacheName + "." + methodName, e);
            } catch (SecurityException e) {
                logger.error("Could not obtain cache clear method: " + cacheName + "." + methodName, e);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("Error while calling clear cache method: " + cacheName + "." + methodName, e);
            }
        }
    }

}
