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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;

public interface WorkflowMirrorService {

	/**
	 * Creates, updates or deletes task mirror.
	 * @param taskId
	 */
	public void mirrorTask(String taskId);
	
	/**
	 * Creates, updates or deletes task mirror asynchronously.
	 * @param taskId
	 */
	public void mirrorTaskAsync(String taskId);
	
	/**
	 * Creates or updates task mirror.
	 * @param task
	 */
	public void mirrorTask(WorkflowTask task);

	/**
	 * Updates or deletes task mirror.
	 * @param task
	 */
	public void mirrorTask(NodeRef task);

    /**
     *  Creates, updates or deletes task mirror for all tasks.
     */
    public void mirrorAllTasks();

    /**
     * Creates, updates or deletes task mirror for all tasks asynchronously.
     */
    public void mirrorAllTasksAsync();

    /**
     * Gets task mirror nodeRef.
     * @param taskId - task id
     * @return - task mirror nodeRef
     */
    public NodeRef getTaskMirror(String taskId);

}
