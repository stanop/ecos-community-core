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
package ru.citeck.ecos.workflow.calendar;

import java.util.Date;

import org.activiti.engine.impl.calendar.BusinessCalendar;

/**
 * This class defines Activiti Business Calendar Alias.
 * This is useful when custom business calendar is needed, but not yet implemented.
 * So we can use standard calendar (i.e. 'duration') with other name.
 *
 * @author Sergey Tiunov
 */
public class ActivitiCalendarAlias extends AbstractActivitiCalendar
{
	private String calendarName;
	private BusinessCalendar calendar;
	
	public void setCalendar(String calendarName) {
		this.calendarName = calendarName;
	}

	@Override
	public Date resolveDuedate(String duedateDescription) {
		// lazy init of calendar - thus registration order is not 
		if(calendar == null) {
			calendar = config.getBusinessCalendarManager().getBusinessCalendar(calendarName);
			if(calendar == null) {
				return null;
			}
		}
		return calendar.resolveDuedate(duedateDescription);
	}


	//TODO: To implement methods
	@Override
	public Date resolveDuedate(String s, int i) {
		return resolveDuedate(s);
	}

	@Override
	public Boolean validateDuedate(String s, int i, Date date, Date date1) {
		return null;
	}

	@Override
	public Date resolveEndDate(String s) {
		return null;
	}

}
