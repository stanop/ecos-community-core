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
package ru.citeck.ecos.constraints.common;

import java.util.Collections;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;

public class BooleanValueConstraint extends AbstractConstraint
{
	private static final String ERR_ILLEGAL_VALUE = "idocs.constraints.boolean.";
	private Boolean requiredValue;

	@Override
	protected void evaluateSingleValue(Object value) {
		if(!requiredValue.equals(value)) {
			throw new ConstraintException(ERR_ILLEGAL_VALUE + requiredValue);
		}
	}
	
	public void setValue(String value) {
		this.requiredValue = Boolean.valueOf(value);
	}
	
	@Override
	public Map<String,Object> getParameters() {
		return Collections.singletonMap("value", (Object) requiredValue);
	}
	
	@Override
	public String toString() {
		return "BooleanValueConstraint[value=" + requiredValue + "]";
	}

}
