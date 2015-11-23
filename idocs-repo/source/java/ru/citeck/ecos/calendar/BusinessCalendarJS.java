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
package ru.citeck.ecos.calendar;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import org.alfresco.service.cmr.search.SearchService;

public class BusinessCalendarJS extends AlfrescoScopableProcessorExtension {

	private static BusinessCalendar impl;
	private static SearchService searchService;

	public static BusinessCalendar getInstance()
	{
		BusinessCalendar cal = impl.getInstance();
		cal.setSearchService(searchService);
		return cal;
	}
	
	public void add(int field, int amount)
	{
		impl.add(field, amount);
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
		//impl.setSearchService(searchService);
	}

	/*public void setWorkingDays() {
		impl.setWorkingDays();
	}

	public void setDayOff() {
		impl.setDayOff();
	}*/

}
