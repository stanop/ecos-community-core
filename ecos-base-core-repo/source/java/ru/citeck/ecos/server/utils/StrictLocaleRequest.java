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
package ru.citeck.ecos.server.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This wrapper changes the Accept-language parameter of the request
 *
 */
public class StrictLocaleRequest extends HttpServletRequestWrapper {

	/**
	 * Constructor. 
	 * 
	 * @param request HttpServletRequest.
	 */
	public StrictLocaleRequest(HttpServletRequest request) {
		super(request);
	}
	
	public String getHeader(String name) {
		//get the request object and cast it
		HttpServletRequest request = (HttpServletRequest)getRequest();
		
		//if we are looking for the "username" request header
		if("Accept-Language".equals(name)) {
			return "ru";
		}
		
		//otherwise fall through to wrapped request object
		return request.getHeader(name);
	}
	

}