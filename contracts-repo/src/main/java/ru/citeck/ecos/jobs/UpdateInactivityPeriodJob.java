package ru.citeck.ecos.jobs;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class UpdateInactivityPeriodJob extends AbstractScheduledLockedJob implements StatefulJob {
    
    public UpdateInactivityPeriodJob() {}
    
    @Override
    public void executeJob(JobExecutionContext jobContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();

        Object executorObj = jobDataMap.get("jobExecutor");

        if (executorObj == null || !(executorObj instanceof UpdateInactivityPeriodExecutor)) {
            throw new AlfrescoRuntimeException("Incorrect type of job");
        }

        UpdateInactivityPeriodExecutor jobExecutor = (UpdateInactivityPeriodExecutor) executorObj;

        AuthenticationUtil.runAsSystem(() -> {
            jobExecutor.execute();
            return null;
        });
    }
}