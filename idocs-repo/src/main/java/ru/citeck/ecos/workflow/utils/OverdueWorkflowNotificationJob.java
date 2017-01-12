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
package ru.citeck.ecos.workflow.utils;

import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import ru.citeck.ecos.notification.NotificationSender;

public class OverdueWorkflowNotificationJob implements Job 
{
	private static Log logger = LogFactory.getLog(OverdueWorkflowNotificationJob.class);

	private static final Object PARAM_NOTIFICATION_SENDER = "NotificationSender";
	private static final Object PARAM_WORKFLOW_SERVICE = "WorkflowService";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();

		final WorkflowService workflowService = (WorkflowService) data.get(PARAM_WORKFLOW_SERVICE);
		final NotificationSender<WorkflowTask> sender = (NotificationSender<WorkflowTask>) data.get(PARAM_NOTIFICATION_SENDER);
		
		Integer sent = AuthenticationUtil.runAs(new RunAsWork<Integer>() {

			@Override
			public Integer doWork() throws Exception {
				logger.debug("OverdueWorkflowNotificationJob start");
				WorkflowTaskQuery query = new WorkflowTaskQuery();
				query.setTaskState(WorkflowTaskState.IN_PROGRESS);
				query.setOrderBy(new OrderBy[] { OrderBy.TaskDue_Asc });
				List<WorkflowTask> tasks = workflowService.queryTasks(query);
				Date now = new Date();
				int sent = 0;
				for(WorkflowTask task : tasks) {
					Date dueDate = task.getPath().getInstance().getDueDate();
					if(dueDate != null)
					{
						dueDate.setHours(23); // use 23:59, not 00:00
						dueDate.setMinutes(59);
							if(dueDate.before(now)) {
								logger.info("Send notification");
								sender.sendNotification(task);
								sent++;
							}
					}
				}
				return sent;
			}
			
		}, AuthenticationUtil.getSystemUserName());
		if(logger.isInfoEnabled()) {
			logger.info("Sent notifications for " + sent + " overdue tasks");
		}
	}

}
