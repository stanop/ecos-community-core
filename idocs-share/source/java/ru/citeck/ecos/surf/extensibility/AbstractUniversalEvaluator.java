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
package ru.citeck.ecos.surf.extensibility;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.ExtensionModuleEvaluator;
import org.springframework.extensions.surf.extensibility.SubComponentEvaluator;

/**
 * Evaluator, that can be used for both sub-components and modules.
 * 
 * @author Sergey Tiunov
 */
public abstract class AbstractUniversalEvaluator implements SubComponentEvaluator, ExtensionModuleEvaluator
{
	private String[] requiredProperties;
    private String beanName;

	@Override
	public boolean evaluate(RequestContext context, Map<String, String> params) {
		return evaluateImpl(context, params);
	}
	
	@Override
	public boolean applyModule(RequestContext context, Map<String, String> evaluationProperties) {
		return evaluateImpl(context, evaluationProperties);
	}
	
	// implementation
	protected abstract boolean evaluateImpl(RequestContext rc, Map<String, String> params);

	@Override
	public String[] getRequiredProperties() {
		return requiredProperties;
	}

	public void setRequiredProperties(String[] requiredProperties) {
		this.requiredProperties = requiredProperties;
	}
	
	public String getBeanName() {
		return beanName;
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
}
