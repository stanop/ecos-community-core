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
package ru.citeck.ecos.spring;

import java.beans.PropertyEditor;
import java.util.Map;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

public class MapPropertyEditorRegistrar implements PropertyEditorRegistrar 
{
	private Map<Class<?>,PropertyEditor> propertyEditors;

	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		for(Class<?> cls : propertyEditors.keySet()) {
			registry.registerCustomEditor(cls, propertyEditors.get(cls));
		}
		
	}

	public void setPropertyEditors(Map<Class<?>,PropertyEditor> propertyEditors) {
		this.propertyEditors = propertyEditors;
	}

}
