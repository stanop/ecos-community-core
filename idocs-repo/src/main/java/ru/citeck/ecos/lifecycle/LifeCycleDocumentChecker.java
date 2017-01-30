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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.NodeRef;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * @author: Alexander Nemerov
 * @date: 22.04.2014
 */
public class LifeCycleDocumentChecker extends AbstractScheduledLockedJob {

    private static final String PARAM_LIFECYCLE_SERVICE = "lifeCycleService";

    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        // Get the lifecycle service from the job map
        Object lifeCycleServiceObj = jobData.get(PARAM_LIFECYCLE_SERVICE);
        if (lifeCycleServiceObj == null || !(lifeCycleServiceObj instanceof LifeCycleService))
        {
            throw new AlfrescoRuntimeException(
                    "ExecuteScriptJob data must contain valid lifecycle service");
        }
        final LifeCycleService lifeCycleService = (LifeCycleService) lifeCycleServiceObj;

        List<NodeRef> docs = lifeCycleService.getDocumentsWithTimer();
        for (final NodeRef doc : docs) {
            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
                @Override
                public Object doWork() throws Exception {
                    lifeCycleService.doTimerTransition(doc);
                    return null;
                }
            });
        }
    }
}
