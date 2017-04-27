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
package ru.citeck.ecos.share.user;

import org.json.JSONObject;
import org.springframework.extensions.surf.site.AlfrescoUser;

/**
 * UserDecorator : is used to add custom properties to User object.
 * 
 * @author Alex
 *
 */
public interface UserDecorator {

	/**
	 * Adds custom properties (specified by implementation) to User object.
	 * Note: json object is the response of webscript 
	 *  /webframework/content/metadata?user={userName}
	 * 
	 * @param user - User object to be decorated
	 * @param json - original json data, used for instantiating User
	 */
	public void decorateUser(AlfrescoUser user, JSONObject json);
	
}
