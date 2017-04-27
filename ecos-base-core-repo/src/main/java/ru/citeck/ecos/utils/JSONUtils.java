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
package ru.citeck.ecos.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {

	public static Object convertJSON(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof org.json.simple.JSONObject) {
		    return convertJSON((org.json.simple.JSONObject) obj);
		}
		if(obj instanceof org.json.simple.JSONArray) {
		    return convertJSON((org.json.simple.JSONArray) obj);
		}
		if(obj instanceof org.json.JSONObject) {
		    return convertJSON((org.json.JSONObject) obj);
		}
		if(obj instanceof org.json.JSONArray) {
		    return convertJSON((org.json.JSONArray) obj);
		}
		if(obj == org.json.JSONObject.NULL) {
		    return null;
		}
		return obj;
	}

    public static List<Object> convertJSON(org.json.JSONArray jsonArray) {
        List<Object> converted = new ArrayList<Object>(jsonArray.length());
        for(int i = 0, ii = jsonArray.length(); i < ii; i++) {
            converted.add(i, convertJSON(jsonArray.opt(i)));
        }
        return converted;
    }

    public static Map<String, Object> convertJSON(org.json.JSONObject jsonObject) {
        Map<String, Object> converted = new HashMap<String, Object>(jsonObject.length());
        for(String name : org.json.JSONObject.getNames(jsonObject)) {
            converted.put(name, convertJSON(jsonObject.opt(name)));
        }
        return converted;
    }

    public static List<Object> convertJSON(org.json.simple.JSONArray jsonArray) {
        List<Object> converted = new ArrayList<Object>(jsonArray.size());
        for(Object child : jsonArray) {
        	converted.add(convertJSON(child));
        }
        return converted;
    }

    public static Map<String, Object> convertJSON(org.json.simple.JSONObject jsonObject) {
        Map<String, Object> converted = new HashMap<String, Object>(jsonObject.size());
        for(Object key : jsonObject.keySet()) {
            converted.put(
                    key != null ? key.toString() : null, 
                    convertJSON(jsonObject.get(key)));
        }
        return converted;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object jsonCopy(Object obj) {
		if(obj instanceof Map) {
			Map map = (Map) obj;
			Map copy = new HashMap(map.size());
			for(Object key : map.keySet()) {
				copy.put(key, jsonCopy(map.get(key)));
			}
			return copy;
		}
		if(obj instanceof List) {
			List list = (List) obj;
			List copy = new ArrayList(list.size());
			for(Object child : list) {
				copy.add(jsonCopy(child));
			}
			return copy;
		}
		return obj;
	}

	public static Object parseJSON(String jsonString) {
		Object jsonObject = org.json.simple.JSONValue.parse(jsonString);
		return convertJSON(jsonObject);
	}

	public static Object prepareToSerialize(Object x) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(x);
			out.close();
			ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
			ObjectInputStream in = new ObjectInputStream(bin);
			Object y = in.readObject();
			return y;
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
}
