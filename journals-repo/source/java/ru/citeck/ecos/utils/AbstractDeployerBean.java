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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

public abstract class AbstractDeployerBean extends AbstractLifecycleBean implements BeanNameAware {

    private Log logger;
    private boolean enabled = true;
    private final String artifactType;
    private List<String> locations;
    private String beanName;
    
    protected AbstractDeployerBean(String artifactType) {
        this.artifactType = artifactType;
        this.logger = LogFactory.getLog(this.getClass());
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setLocation(String location) {
        this.locations = Collections.singletonList(location);
    }
    
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public void load() {
        if(locations == null || locations.isEmpty()) {
            logger.info(beanName + ": nothing to deploy");
            return;
        }
        
        logger.info(beanName + ": deploying " + artifactType + " (" + locations.size() + " locations)");
        
        for(String location : locations) {
            if(logger.isDebugEnabled()) {
                logger.debug(beanName + ": deploying " + artifactType + ": " + location);
            }
            load(location);
            if(logger.isDebugEnabled()) {
                logger.debug(beanName + ": successfully deployed " + artifactType + ": " + location);
            }
        }
    }

    public void load(String location) {
        try {
            // default protocol is classpath
            Resource resource = location.contains(":")
                    ? new UrlResource(location)
                    : new ClassPathResource(location);
            load(resource.getURL().toString(), resource.getInputStream());
        } catch(Exception e) {
            throw new IllegalStateException("Could not deploy " + artifactType + ", location: " + location + ", deployer: " + beanName, e);
        }
    }

    protected abstract void load(String location, InputStream inputStream);

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        if(enabled) {
            load();
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // NOOP
    }
    
    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

}
