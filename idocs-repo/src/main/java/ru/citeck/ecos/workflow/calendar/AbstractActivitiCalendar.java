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

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.apache.log4j.Logger;

/**
 * Abstract Activiti Business Calendar.
 * BusinessCalendar is responsible for calculating due-date, given an expression of it.
 * Different implementations of BusinessCalendar can support various expressions.
 * This class supports registering of subclasses in Activiti ProcessEngineConfiguration by name.
 * 
 * @author Sergey Tiunov
 *
 */
public abstract class AbstractActivitiCalendar implements BusinessCalendar
{
	private final String CANT_INIT_MSG = "Could not initialize activiti calendar {0}: {1}";
	private Logger logger = Logger.getLogger(BusinessCalendar.class);
	protected String name;
	protected ProcessEngineConfigurationImpl config;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl config) {
		this.config = config;
	}

	public void init() {
		if(name == null) {
			logger.warn(String.format(CANT_INIT_MSG, "", "name is not specified"));
			return;
		}
		if(config == null) {
			logger.warn(String.format(CANT_INIT_MSG, name, 
					"ProcessEngineConfiguration is not specified"));
			return;
		}
		if(!(config.getBusinessCalendarManager() instanceof MapBusinessCalendarManager)) {
			logger.warn(String.format(CANT_INIT_MSG, name, 
					"BusinessCalendarManager is not MapBusinessCalendarManager, but instead is " + 
					config.getBusinessCalendarManager().getClass().getName()));
			return;
		}
		MapBusinessCalendarManager manager = (MapBusinessCalendarManager) config.getBusinessCalendarManager();
		manager.addBusinessCalendar(name, this);
	}

}
