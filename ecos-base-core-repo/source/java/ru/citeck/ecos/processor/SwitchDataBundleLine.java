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
package ru.citeck.ecos.processor;

import java.util.Map;

/**
 * Switch Data Bundle Line is a Data Bundle Line, that acts like switch construct.
 * It evaluates given expression and runs Data Bundle Line, that corresponds to its value.
 * More precisely if the value is contained in 'cases' map, then Data Bundle Line taken from this map.
 * Otherwise default Data Bundle Line is called.
 * 
 * Switch Data Bundle Line can be used to include conditional logic into processor chain.
 * 
 * The format of expression depends on the used Expression Evaluator.
 * 
 * @author Sergey Tiunov
 *
 */
public class SwitchDataBundleLine extends AbstractDataBundleLine
{
	private String expression;
	private Map<String, DataBundleLine> cases;
	private DataBundleLine defaultLine;

	@Override
	public DataBundle process(DataBundle input) {
		
		Object value = super.evaluateExpression(expression, input);
		if(cases.containsKey(value)) {
			DataBundleLine caseProcessor = cases.get(value);
			if(caseProcessor != null) {
				return caseProcessor.process(input);
			} else {
				return input;
			}
			
		} else if(defaultLine != null) {
			
			return defaultLine.process(input);
			
		} else {
			
			return input;
			
		}
	}

	/**
	 * Set the expression of the switch.
	 * @param expression
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Set the cases of the switch.
	 * 
	 * @param cases
	 */
	public void setCases(Map<String, DataBundleLine> cases) {
		this.cases = cases;
	}

	/**
	 * Set default Data Bundle Line of the switch.
	 * @param defaultLine
	 */
	public void setDefault(DataBundleLine defaultLine) {
		this.defaultLine = defaultLine;
	}

}
