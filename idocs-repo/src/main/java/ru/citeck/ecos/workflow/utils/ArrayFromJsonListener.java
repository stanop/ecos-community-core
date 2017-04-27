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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ru.citeck.ecos.workflow.listeners.AbstractExecutionListener;

/**
 * 
 * @author Ruslan
 *
 */
public class ArrayFromJsonListener extends AbstractExecutionListener {

	private static Log log = LogFactory.getLog(ArrayFromJsonListener.class);

	private Expression var;
	private Expression json;
	private Expression path;

	@Override
	protected void notifyImpl(DelegateExecution execution) throws Exception {
		List<Object> result = new ArrayList<Object>();
		String variableName = (String)var.getValue(execution);
		Object jsonObject = json.getValue(execution);
		String pathLine = (String)path.getValue(execution);

		extractElements(jsonObject, pathLine, result);
		execution.setVariable(variableName, result);
	}

	private static void extractElements(Object json, String path, List<Object> result) {
		if (path == null || json == null || path.length() == 0)
			return;
		String element = null;
		String pathRest = null;
		int pos = path.indexOf(".");
		if (pos < 0) {
			element = path;
		}
		else if (pos == 0) {
			if (path.length() > 0)
				pathRest = path.substring(1);
		}
		else {
			element = path.substring(0, pos);
			pathRest = path.substring(pos + 1);
		}
		if (element == null || element.length() == 0) {
			if (pathRest == null || pathRest.isEmpty())
				result.add(json);
			else
				extractElements(json, pathRest, result);
		}
		else {
			if (json instanceof JSONObject) {
				JSONObject json2 = (JSONObject)json;
				Object v = json2.get(element);
				if (v != null) {
					if (pathRest == null || pathRest.isEmpty()) {
						result.add(v);
					}
					else {
						extractElements(v, pathRest, result);
					}
				}
			}
			else if (json instanceof JSONArray) {
				JSONArray json2 = (JSONArray)json;
				for (Object v : json2)
					extractElements(v, path, result);
			}
			else {
				if (log.isDebugEnabled())
					log.debug("Can not extract element for class=" + json.getClass().getName());
			}
		}
	}

}
