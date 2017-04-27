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
package ru.citeck.ecos.webscripts.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class WebScriptUtils {
	
	/**
	 * Get webscript arguments map.
	 * It is the same as args array in javascript.
	 * @param req web script request
	 * @return map: argument name -> argument value 
	 */
	public static Map<String,String> getParameterMap(WebScriptRequest req) {
		String[] keys = req.getParameterNames();
		Map<String,String> args = new HashMap<String,String>(keys.length);
		for(String key : keys) {
			args.put(key, req.getParameter(key));
		}
		return args;
	}

	/**
	 * Get webscript body as json.
	 * It is the same as json object in javascript.
	 * @param req web script request
	 * @return json representation of body, or null if any error occur
	 */
	public static Object getContentJSON(WebScriptRequest req) {
    	Reader contentReader = null;
    	try {
    		contentReader = req.getContent().getReader();
	    	return JSONValue.parse(contentReader);
    	} catch(IOException e) {
    		// do nothing
    	}
    	return null;
	}

}
