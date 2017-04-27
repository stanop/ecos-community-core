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

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.site.AlfrescoUser;

/**
 * Property UserDecorator : adds new properties to User.
 * Properties are specified by jsonPath, that is list of keys for json data object.
 * 
 * @author Alex
 *
 */
public class PropertyUserDecorator extends AbstractUserDecorator
{
	private String propertyName;
	private List<String> jsonPath;

	@Override
	public void decorateUser(AlfrescoUser user, JSONObject json) {
		Object value = json;
		for(String key : jsonPath) {
			if(value == null) {
				break;
			} else if(value instanceof JSONObject) {
				try {
					value = ((JSONObject) value).get(key);
				} catch (JSONException e) {
					value = null;
				}
			} else if(value instanceof JSONArray) {
				try {
					value = ((JSONArray) value).get(Integer.parseInt(key));
				} catch (NumberFormatException e) {
					value = null;
				}
			} else {
				value = null;
			}
		}
		user.setProperty(propertyName, (Serializable) value);
	}

	/**
	 * Set name of property to be set in User object.
	 * 
	 * @param propertyName
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Set path in json object.
	 * Note: path can contain both keys (for JSONObject) and indexes (for JSONArray).
	 * 
	 * @param jsonPath
	 */
	public void setJsonPath(List<String> jsonPath) {
		this.jsonPath = jsonPath;
	}

}
