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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.web.site.SlingshotUserFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.site.AlfrescoUser;

/**
 * User factory for Slingshot, that allows created User object 
 *  to be decorated by various UserDecorators.
 * 
 * @author Sergey Tiunov
 */
public class DecoratedUserFactory extends SlingshotUserFactory
{
	private List<UserDecorator> userDecorators = new ArrayList<UserDecorator>();

	@Override
    protected AlfrescoUser buildAlfrescoUser(JSONObject json)
            throws JSONException, UserFactoryException
    {
        AlfrescoUser user = super.buildAlfrescoUser(json);
        for(UserDecorator decorator : userDecorators) {
        	decorator.decorateUser(user, json);
        }
        return user;
    }
	
	/**
	 * Register new UserDecorator.
	 * All registered user decorators will be invoked, after creating user.
	 * 
	 * @param decorator
	 */
	public void registerDecorator(UserDecorator decorator) {
		userDecorators.add(decorator);
	}

}
