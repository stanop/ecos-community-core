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
 * Data Access Object for group sub-types.
 * 
 * @author Sergey Tiunov
 *
 */
public interface GroupSubTypeDAO {

	/**
	 * Get group sub-type.
	 * @param name
	 * @return
	 */
	NodeRef getSubType(String name);
	
	/**
	 * Get all group sub-types.
	 * @return
	 */
	List<NodeRef> getAllSubTypes();

	/**
	 * Create group sub-type.
	 * @param name
	 * @return
	 */
	NodeRef createSubType(String name);

	/**
	 * Delete group sub-type.
	 * @param name
	 */
	void deleteSubType(String name);
	
	/**
	 * Get sub-types root nodeRef.
	 * This is where all sub-types are stored.
	 * @return
	 */
	NodeRef getSubTypeRoot();
}
