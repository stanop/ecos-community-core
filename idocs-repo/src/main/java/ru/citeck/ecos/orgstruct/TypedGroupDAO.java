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

import java.util.Collection;
import java.util.List;

/**
 * Data Access Object for typed groups.
 * Typed groups are, for example, branches and roles.
 * Thus, there will be one DAO for branches, one for roles, and so on.
 * 
 * @author Sergey Tiunov
 *
 */
public interface TypedGroupDAO {

	/**
	 * Determines, whether specified group is of this DAO type.
	 * @param name
	 * @return
	 */
	boolean isTypedGroup(String name);
	
	/**
	 * Get subtype-name of typed-group.
	 * @param name - group name
	 * @return group subtype name (if it is a typed group)
	 */
	String getGroupSubtype(String name);

	/**
	 * Get all typed groups of this DAO type.
	 * @param rootOnly
	 * @return
	 */
	@Deprecated
	List<String> getAllTypedGroups(boolean rootOnly);
	
	/**
	 * Get all typed groups of this DAO type and specified subtype.
	 * @param sub-type
	 * @param rootOnly
	 * @return
	 */
	@Deprecated
	List<String> getAllTypedGroups(String subtype, boolean rootOnly);
	
	/**
	 * Perform filtering of groups, leaves only groups, 
	 *  that correspond to group type and group subtype (if specified).
	 * @param groups
	 * @param subtype - required subtype of filtered groups, can be null
	 * @return - filtered list of groups
	 */
	List<String> filterTypedGroups(Collection<String> groups, String subtype);
	
	/**
	 * Create typed group of this DAO type.
	 * @param subtype
	 * @param name
	 * @return
	 */
	String createTypedGroup(String subtype, String name);

	/**
	 * Delete specified typed group.
	 * @param name
	 */
	void deleteTypedGroup(String name);

	/**
	 * Convert specified typed group to simple (non-typed) group.
	 * @param name
	 */
	void convertToSimpleGroup(String name);

}
