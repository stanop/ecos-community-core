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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * @author Alexey Moiseev
 * @date 30.08.2015.
 */
public class LifeCycleRepositoryDeployer extends AbstractLifecycleBean {

    private LifeCycleService lifeCycleService;

    private boolean enabled;

    private static Log logger = LogFactory.getLog(LifeCycleRepositoryDeployer.class);

    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    public void setEnabled(boolean enableUploader) {
        this.enabled = enableUploader;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        if (!enabled)
            return;

        logger.info("Deploying lifecycles from repository...");

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() {
                try {
                    lifeCycleService.deployStoredLifeCycles();
                } catch (Exception e) {
                    logger.warn("Could not deploy lifecycles from repository", e);
                }
                return null;
            }
        });

        logger.info("Successfully deployed lifecycles from reposotory");
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {

    }

}
