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
package ru.citeck.ecos.workflow.mirror;

import org.activiti.engine.delegate.DelegateTask;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.workflow.listeners.AbstractTaskListener;

public class MirrorListener extends AbstractTaskListener {

	private WorkflowMirrorService service;
	
	@Override
	protected void notifyImpl(DelegateTask task) {
        final String taskId = "activiti$" + task.getId();
        //service.mirrorTaskAsync(taskId);
        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {

                // ensure, that it is executed after all behaviours
                AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                    public void beforeCommit(boolean readOnly) {
                        service.mirrorTask(taskId);
                    }
                });

            }
        });
    }

	public void initImpl() {
		if(service == null) {
			service = (WorkflowMirrorService) serviceRegistry.getService(CiteckServices.WORKFLOW_MIRROR_SERVICE);
		}
	}
	
}
