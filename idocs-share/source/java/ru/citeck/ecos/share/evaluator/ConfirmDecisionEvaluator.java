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

public class ConfirmDecisionEvaluator extends BaseEvaluator {

	private Comparator comparator;
	
	@Override
	public boolean evaluate(JSONObject object) {
		
		// deserialize confirm decisions:
		JSONObject decisions = ConfirmUtil.deserializeConsideredVersions(object, this);
		
		// current user
		RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		String userName = rc.getUserId();
		
		// look for his confirm decision
		Object decision = this.getJSONValue(decisions, userName + ".decision");

		// run comparator
		return comparator.compare(decision);
	}

	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

}
