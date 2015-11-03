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

import org.alfresco.service.ServiceRegistry;

/**
 * Abstract Data Bundle Processor - base class for all processors.
 * It has all necessary utility methods.
 * 
 * @author Sergey Tiunov
 *
 */
public abstract class AbstractDataBundleProcessor implements DataBundleProcessor
{

	protected ServiceRegistry serviceRegistry;
	protected ProcessorHelper helper;
	private ExpressionEvaluator evaluator;
	
	public void init() {
		// this can be overriden by children
	}
	
	protected Object evaluateExpression(String expression, Map<String, Object> model) {
		return evaluator.evaluate(expression, model);
	}
	
	protected Object evaluateExpression(String expression, DataBundle dataBundle) {
		return evaluateExpression(expression, dataBundle.getModel());
	}
	
	public final void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setHelper(ProcessorHelper helper) {
		this.helper = helper;
	}

	public void setEvaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

}
