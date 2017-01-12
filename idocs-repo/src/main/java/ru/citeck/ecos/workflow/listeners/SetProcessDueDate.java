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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;

/**
 * Set process due-date Activiti execution-listener.
 * Due-date expression is interpreted by Activiti calendar, 
 * that is specified in 'calendar' property.
 * Calculated due date is set to specified 'variable'.
 * 
 * @author Sergey Tiunov
 */
public class SetProcessDueDate implements ExecutionListener {

	private DueDateHelper helper = new DueDateHelper();
	private Expression calendar;
	private Expression variable;
	private Expression dueDate;
	
	@Override
	public void notify(DelegateExecution execution) {
		String calendarName = this.calendar != null ? this.calendar.getValue(execution).toString() : DurationBusinessCalendar.NAME;
		String dueDateExpr = (String) dueDate.getValue(execution);
		Date dueDateValue = helper.getDueDate(calendarName, dueDateExpr);
		execution.setVariable(variable.getValue(execution).toString(), dueDateValue);
	}
	
}
