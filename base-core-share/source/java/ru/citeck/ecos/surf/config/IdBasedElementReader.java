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

import org.dom4j.Element;
import org.springframework.extensions.config.element.GenericConfigElement;
import org.springframework.extensions.config.xml.elementreader.GenericElementReader;

public class IdBasedElementReader extends GenericElementReader {

	public IdBasedElementReader() {
		super(null);
	}

	@Override
	protected IdBasedElement createConfigElement(Element element) {
		GenericConfigElement configElement = super.createConfigElement(element);
		return new IdBasedElement(configElement);
	}
	
}
