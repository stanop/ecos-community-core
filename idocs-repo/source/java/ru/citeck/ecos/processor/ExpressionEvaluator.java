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
 * Expression Evaluators can be used to evaluate expressions in some format, given the model (a map of variables).
 * The specific format of expressions is a subject of implementation.
 * 
 * @author Sergey Tiunov
 */
public interface ExpressionEvaluator {
	
	/**
	 * Evaluate expression, given a model - map of variables.
	 * 
	 * @param expression
	 * @param model
	 * @return evaluated result
	 */
	Object evaluate(String expression, Map<String, Object> model);
	
}
