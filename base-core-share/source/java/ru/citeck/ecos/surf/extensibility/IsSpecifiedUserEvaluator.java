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

public class IsSpecifiedUserEvaluator extends AbstractUniversalEvaluator {
	private static final String USER_NAMES = "userNames";
	private static final String INVERSE = "inverse";

	@Override
	protected boolean evaluateImpl(RequestContext rc, Map<String, String> params) {
		boolean result = false;
		String userNames = params.get(USER_NAMES);
		String userId = rc.getUserId();
		if (isNotEmpty(userNames) && isNotEmpty(userId)) {
			String[] names = userNames.split(",");
			for (String name : names) {
				name = name.trim();
				if (userId.equals(name)) {
					result = true;
					break;
				}
			}
		}
		String inverseStr = params.get(INVERSE);
		if (isNotEmpty(inverseStr)) {
			if (Boolean.parseBoolean(inverseStr))
				result = !result;
		}
		return result;
	}

	private boolean isNotEmpty(String value) {
		return value != null && value.length() > 0;
	}

}
