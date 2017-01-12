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
package ru.citeck.ecos.behavior.content;

import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * This class helps to initialize map {@code sizeLimits} of
 * class {@code ContentNodeBehaviorImpl} in modules defined in
 * another packages.
 * 
 * @author Ruslan
 *
 */
public class ContentNodeSizeLimitsRegistrar {

	private ContentNodeBehavior contentNodeBehavior;
	private Map<QName, Long> sizeLimits;

	public void init() {
		if (contentNodeBehavior != null && sizeLimits != null) {
			contentNodeBehavior.registerSizeLimits(sizeLimits);
		}
	}

	public void setContentNodeBehavior(ContentNodeBehavior contentNodeBehavior) {
		this.contentNodeBehavior = contentNodeBehavior;
	}

	public void setSizeLimits(Map<QName, Long> sizeLimits) {
		this.sizeLimits = sizeLimits;
	}

}
