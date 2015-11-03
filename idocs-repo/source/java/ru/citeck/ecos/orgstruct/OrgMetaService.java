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
package ru.citeck.ecos.orgstruct;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Java interface for orgstruct meta service.
 * 
 * @author Sergey Tiunov
 *
 */
public interface OrgMetaService extends OrgMetaServiceGeneric<NodeRef, List<NodeRef>>
{

	/**
	 * Gets list of supported group types.
	 * @return
	 */
	List<String> getGroupTypes();
	
}
