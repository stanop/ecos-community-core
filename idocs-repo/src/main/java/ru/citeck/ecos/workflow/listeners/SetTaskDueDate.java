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
package ru.citeck.ecos.workflow.listeners;

import java.util.Date;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;

/**
 * Set task due-date Activiti task-listener.
 * Due-date expression is interpreted by Activiti calendar, 
 * that is specified in calendar property.
 * Calculated due-date is set in task.
 * 
 * @author Sergey Tiunov
 */
public class SetTaskDueDate implements TaskListener {

	private DueDateHelper helper = new DueDateHelper();
	private Expression calendar;
	private Expression dueDate;
	
	@Override
	public void notify(DelegateTask task) {
		String calendarName = this.calendar != null ? this.calendar.getValue(task).toString() : DurationBusinessCalendar.NAME;
		String dueDateExpr = (String) dueDate.getValue(task);
		Date dueDateValue = helper.getDueDate(calendarName, dueDateExpr);
		task.setDueDate(dueDateValue);
	}
	
}
