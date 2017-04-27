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
package ru.citeck.ecos.lifecycle;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * @author Alexander Nemerov
 * created on 31.03.2015.
 */
public class LifeCycleDeployer extends AbstractLifecycleBean {

    private LifeCycleService lifeCycleService;

    private boolean enabled;

    private String location;
    private String format;
    private QName entityType;

    private static Log logger = LogFactory.getLog(LifeCycleDeployer.class);

    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    public void setEnabled(boolean enableUploader) {
        this.enabled = enableUploader;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setEntityType(QName entityType) {
        this.entityType = entityType;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        if (!enabled)
            return;

        logger.info("Deploying lifecycle: " + location);

        final ClassPathResource lifeCycleResource = new ClassPathResource(location);

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() {
                InputStream lifeCycleDefinition;
                try {
                    lifeCycleDefinition = lifeCycleResource.getInputStream();
                    lifeCycleService.deployLifeCycle(lifeCycleDefinition, format, entityType, location);

                    // temp storing to repo
                    //StringWriter writer = new StringWriter();
                    //IOUtils.copy(lifeCycleResource.getInputStream(), writer, "UTF-8");
                    //String content = writer.toString();
                    //lifeCycleService.storeLifeCycleDefinition(null, content, format, entityType, location, false);
                    // end temp
                } catch (IOException e) {
                    logger.warn("Could not deploy lifecycle", e);
                }
                return null;
            }
        });

        logger.info("Successfully deployed lifecycle: " + location);
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // NOOP
    }

}
