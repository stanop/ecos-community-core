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
package ru.citeck.ecos.archive;

import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;

/**
 * It allows move documents into the archive.
 * 
 * @author Ruslan Vildanov
 *
 */
public interface ArchiveServiceGeneric<NodeRef> {

	/**
	 * It moves specified node into the archive.
	 * 
	 * @param nodeRef - document node, which is moving to the archive
	 * @param cause - it can takes any value. This value comes into behavior
	 * catcher as a parameter. So you can do any things in a behavior class
	 * depends on <code>cause</code> of moving node of a same type.
	 * @throws InvalidNodeRefException
	 * @throws CyclicChildRelationshipException
	 */
	void move(NodeRef nodeRef, String cause)
			throws InvalidNodeRefException, CyclicChildRelationshipException;

	void clearCache();

}
