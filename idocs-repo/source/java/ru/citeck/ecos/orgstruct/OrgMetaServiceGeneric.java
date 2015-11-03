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

/**
 * Generic interface for orgstruct meta service.
 * Contains basic CRUD for group subtypes.
 * 
 * @author Sergey Tiunov
 *
 * @param <NodeType> type that represent node (this can be NodeRef, ScriptNode, etc.)
 * @param <GroupType> type that represent list of NodeType nodes
 */
public interface OrgMetaServiceGeneric<NodeType, GroupType> {

	/**
	 * Get group sub-type.
	 * @param type - group type
	 * @param name - group sub-type name
	 * @return
	 */
	NodeType getSubType(String type, String name);

	/**
	 * Get all group sub-types of specified group type.
	 * @param type - group type
	 * @return
	 */
	GroupType getAllSubTypes(String type);
	
	/**
	 * Create group sub-type of specified group type.
	 * @param type - group type.
	 * @param name - group sub-type name.
	 * @return
	 */
	NodeType createSubType(String type, String name);

	/**
	 * Delete group sub-type of specified group type.
	 * @param type - group type.
	 * @param name - group sub-type name.
	 */
	void deleteSubType(String type, String name);
	
	/**
	 * Gets sub-type root for the given group type.
	 * @param type
	 * @return
	 */
	NodeType getSubTypeRoot(String type);

}
