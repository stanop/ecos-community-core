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

import org.alfresco.web.evaluator.BaseEvaluator;
import org.alfresco.web.evaluator.Comparator;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;

/**
 * Evaluator of URL parameter (argument).
 * For example, in request:
 *   /share/service/...?param1=value1&param2=value2
 * we can confirm that param1 == value1.
 * 
 * @author Sergey Tiunov
 */
public class UrlParameterEvaluator extends BaseEvaluator {
	
	// parameter name
	private String accessor;
	
	// comparator object, that knows correct value
	private Comparator comparator;

	@Override
	public boolean evaluate(JSONObject jsonObject) {
		final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		String parameterValue = rc.getParameter(accessor);
		return comparator.compare(parameterValue);
	}

	public void setAccessor(String accessor) {
		this.accessor = accessor;
	}

	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

}
