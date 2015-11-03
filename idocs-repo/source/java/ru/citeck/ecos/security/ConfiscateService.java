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
package ru.citeck.ecos.security;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Confiscate Service - allows to temporarily confiscate nodes from its owners.
 * It helps to build workflows, where node should not be accessible by others while it is in work.
 * 
 * @author Sergey Tiunov
 */
public interface ConfiscateService 
{
	/**
	 * Confiscate node from its normal users.
	 * 
	 * @param nodeRef
	 */
	public void confiscateNode(NodeRef nodeRef);
	
	/**
	 * Return previously confiscated node to its normal users.
	 * 
	 * @param nodeRef
	 */
	public void returnNode(NodeRef nodeRef);

}
