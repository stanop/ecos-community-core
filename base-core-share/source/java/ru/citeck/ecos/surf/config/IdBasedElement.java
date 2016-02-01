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
package ru.citeck.ecos.surf.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.element.GenericConfigElement;

public class IdBasedElement extends GenericConfigElement {
	
	private static final long serialVersionUID = 2925925928223078364L;
	
	private static final String ID = "id";
	private static final String REPLACE = "replace";
	private static final Object TRUE = "true";
	private Map<String, ConfigElement> childMap = new HashMap<String, ConfigElement>();

	public IdBasedElement(String name) {
		super(name);
	}
	
	public IdBasedElement(ConfigElement that) {
		super(that.getName());
		this.setValue(that.getValue());
		this.addAttributes(that);
		this.addChildren(that);
	}

	@Override
	public ConfigElement combine(ConfigElement that) {
		IdBasedElement result = new IdBasedElement(that.getName());
		result.setValue(that.getValue());
		
		result.addAttributes(this);
		result.addAttributes(that);
		
		result.addChildren(this);
		result.addChildren(that);
		
		return result;
	}
	
	public void addAttributes(ConfigElement that) {
		Map<String, String> attributes = that.getAttributes();
		if(attributes == null) {
			return;
		}
		for(String key : attributes.keySet()) {
			addAttribute(key, attributes.get(key));
		}
	}
	
	public void addChildren(ConfigElement that) {
		List<ConfigElement> children = that.getChildren();
		if(children == null) {
			return;
		}
		for(ConfigElement child : children) {
			addChild(child);
		}
	}

	@Override
	public void addChild(ConfigElement child) {
		String id = child.getAttribute(ID);
        ConfigElement existingChild = null;
        if (id != null) {
            existingChild = childMap.get(id);
        }
		if(existingChild == null) {
			children.add(child);
			childMap.put(id, child);
		} else if(TRUE.equals(child.getAttribute(REPLACE))) {
			Collections.replaceAll(children, existingChild, child);
			childMap.put(id, child);
		} else {
			ConfigElement combinedChild = existingChild.combine(child);
			Collections.replaceAll(children, existingChild, combinedChild);
			childMap.put(id, combinedChild);
		}
	}
	
}
