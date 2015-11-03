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

import java.util.ArrayList;
import java.util.Arrays;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

public class SplitVariableTask implements ExecutionListener {

	private Expression source;
	private Expression target;
	private Expression separator;
	private Expression removeEmpty;
	
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		String sourceVariableName = (String)source.getValue(execution);
		String targetVariableName = (String)target.getValue(execution);
		String sourceValue = (String) execution.getVariable(sourceVariableName);
		ArrayList<String> target = null;
		if(sourceValue.length() == 0) {
			target = new ArrayList<String>();
		} else {
			String separatorString = (String) separator.getValue(execution);
			String[] parts = sourceValue.split("\\" + separatorString);
			target = new ArrayList<String>(parts.length);
			target.addAll(Arrays.asList(parts));
		}
		// remove empty values
		if(removeEmpty != null && removeEmpty.getValue(execution).toString().equals("true")) {
			target.removeAll(Arrays.asList(new String[] { "" }));
		}
		execution.setVariable(targetVariableName, target);
	}

}
