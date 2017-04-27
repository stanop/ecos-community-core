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
package ru.citeck.ecos.share.evaluator;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.web.evaluator.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

/**
 * It expected date in ISO-8601 format in argument of 
 * {@link CurrentDateGreaterComparator#compare(Object)} method. It compares
 * current date with input date.
 * 
 * If current date is greater the input date,
 * returns <code>true</code>, in the other
 * case <code>false</code>.
 * 
 * @author Ruslan
 *
 */
public class CurrentDateGreaterComparator implements Comparator {
	private static final Log log = LogFactory.getLog(CurrentDateGreaterComparator.class);

	private boolean toStart = false;
	private boolean toEnd = false;

	@Override
	public boolean compare(Object arg0) {
		boolean result = false;
		if (arg0 instanceof JSONObject) {
			try {
				if (log.isDebugEnabled()) {
					log.debug(CurrentDateGreaterComparator.class.getName() +
							": Comparing input date with current. Input date=" + arg0);
				}

				JSONObject json = (JSONObject)arg0;
				String input = (String)json.get("iso8601");
				Date inputDate = ISO8601DateFormat.parse(input);
				if (toStart) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(inputDate);
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					inputDate = calendar.getTime();
				}
				else if (toEnd) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(inputDate);
					calendar.set(Calendar.HOUR_OF_DAY, 23);
					calendar.set(Calendar.MINUTE, 59);
					calendar.set(Calendar.SECOND, 59);
					calendar.set(Calendar.MILLISECOND, 999);
					inputDate = calendar.getTime();
				}
				result = (new Date()).compareTo(inputDate) > 0;
			}
			catch(Throwable e) {
				if (log.isErrorEnabled()) {
					log.error(CurrentDateGreaterComparator.class.getName() +
							": Can not compare current date with input date. Input date=" + arg0 +
							"; error=" + e);
				}
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug(CurrentDateGreaterComparator.class.getName() +
						": input argument=" + arg0 +
						"; agument class=" + arg0 == null ? "null" : arg0.getClass().getName());
			}
		}
		return result;
	}

	/**
	 * If it is true, input date will be translated to start of the day.
	 * @param toStart
	 */
	public void setToStart(boolean toStart) {
		this.toStart = toStart;
	}

	/**
	 * If it is true, input date will be translated to end of the day.
	 * @param toEnd
	 */
	public void setToEnd(boolean toEnd) {
		this.toEnd = toEnd;
	}

}
