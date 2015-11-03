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
package ru.citeck.ecos.cardlet;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CardletService {

	public static final String DEFAULT_MODE = "";
	public static final String ALL_MODES = "all";

	/**
	 * Query card modes for specified object and current user.
	 * 
	 * @param nodeRef nodeRef of object
	 * @return list of card mode nodeRefs
	 */
	public List<NodeRef> queryCardModes(NodeRef nodeRef);
	
	/**
	 * Query cardlets for specified object, default card mode and current user.
	 * 
	 * @param nodeRef nodeRef of object
	 * @param cardMode card mode
	 * @return list of cardlet nodeRefs
	 */
	public List<NodeRef> queryCardlets(NodeRef nodeRef);
	
	/**
	 * Query cardlets for specified object, specified card mode and current user.
	 * In cardMode param user can pass null, it means default card mode, 
	 * or {@link ALL_MODES}, it queries cardlets, applicable for all modes (i.e. no mode filtering at all).
	 * 
	 * @param nodeRef nodeRef of object
	 * @param cardMode name of card mode
	 * @return list of cardlet nodeRefs
	 */
	public List<NodeRef> queryCardlets(NodeRef nodeRef, String cardMode);

}
