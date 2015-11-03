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
package ru.citeck.ecos.workflow.confirm;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ru.citeck.ecos.workflow.listeners.AbstractExecutionListener;

public class PrecedenceToJsonListener extends AbstractExecutionListener {

	private Expression var;
	private Expression precedence;

	@SuppressWarnings("unchecked")
	@Override
	protected void notifyImpl(DelegateExecution execution) throws Exception {
		String variableName = (String) var.getValue(execution);
		String precedenceLine = (String) precedence.getValue(execution);
		//precedenceLine
		JSONObject result = new JSONObject();
		JSONArray stages = new JSONArray();
		result.put("stages", stages);
		if (precedenceLine != null && precedenceLine.length() != 0) {
			String[] stageLines = precedenceLine.split("[,]");
			for (String line : stageLines) {
				JSONObject stage = new JSONObject();

				// due date is not supported in old format
				stage.put("dueDate", 0L);
				JSONArray confirmers = new JSONArray();
				stage.put("confirmers", confirmers);
				String[] confirmLines = line.split("[|]");
				for (String confirmer : confirmLines) {
					if (confirmer.length() != 0) {
						JSONObject conf = new JSONObject();
						conf.put("nodeRef", confirmer);
						// full name is not supported in old format
						conf.put("fullName", confirmer);
						// 'can cancel' is not supported in old format
						conf.put("canCancel", false);
						confirmers.add(conf);
					}
				}
				stages.add(stage);
			}
		}
		execution.setVariable(variableName, result);
	}

}
