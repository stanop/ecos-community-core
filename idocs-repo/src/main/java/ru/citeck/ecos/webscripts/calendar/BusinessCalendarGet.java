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
package ru.citeck.ecos.webscripts.calendar;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.alfresco.service.cmr.search.SearchService;

import ru.citeck.ecos.calendar.BusinessCalendar;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.extensions.webscripts.WebScriptException;

public class BusinessCalendarGet extends DeclarativeWebScript
{

	// web script arguments
	private static final String PARAM_ADD_FIELD = "addField";
	private static final String PARAM_ADD_AMOUNT = "addAmount";
	private static final String PARAM_CURRENT_DATE = "currentDate";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	private SearchService searchService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {

        String add_field = req.getParameter(PARAM_ADD_FIELD);
        String add_amount = req.getParameter(PARAM_ADD_AMOUNT);
		String current_date = req.getParameter(PARAM_CURRENT_DATE);


        if(add_field == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter addField is mandatory");
            return null;
        }

        if(add_amount == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter addAmount is mandatory");
            return null;
        }
		
		Date currentDate = null;
		try 
		{
			currentDate = dateFormat.parse(current_date);

		} 
		catch (ParseException ex) 
		{
			throw new WebScriptException("Unable to parse date: " + ex.getMessage(), ex);
		}
		
		int amount = Integer.parseInt(add_amount);

        Map<String, Object> model = new HashMap<String, Object>();
		BusinessCalendar calendar = new BusinessCalendar();
		calendar.setSearchService(searchService);
		calendar.setWorkingDays();
		calendar.setDayOff();
		calendar.setTime(currentDate);
		if("days".equals(add_field))
		{
			calendar.add(Calendar.DAY_OF_YEAR, amount);
			model.put("date", calendar.getTime());
		}
        return model;
    	
    }

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
}
