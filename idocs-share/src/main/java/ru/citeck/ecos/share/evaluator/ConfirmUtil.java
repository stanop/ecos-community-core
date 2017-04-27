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
package ru.citeck.ecos.share.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class ConfirmUtil {

	private static String[] partNames = new String[] { 
		"user", 
		"versionRef", 
		"versionLabel", 
		"decision" 
	};
	private static String consideredVersionsAccessor = "node.properties.wfcf:consideredVersions";
	private static String considerableVersionsAccessor = "node.properties.wfcf:considerableVersions";

	@SuppressWarnings("unchecked")
	public static JSONObject deserializeConsideredVersions(JSONObject node, BaseEvaluator evaluator) {
        return deserializeVersions(node, evaluator, consideredVersionsAccessor);
	}

    public static JSONObject deserializeConsiderableVersions(JSONObject node, BaseEvaluator evaluator) {
        return deserializeVersions(node, evaluator, considerableVersionsAccessor);
    }

    public static JSONObject deserializeVersions(JSONObject node, BaseEvaluator evaluator, String considerableVersionsAccessor) {
        String field = "confirmations";
        JSONObject object = (JSONObject) node.get(field);
        if(object != null) {
            return object;
        }

        String value = (String) evaluator.getJSONValue(node, considerableVersionsAccessor);

        object = new JSONObject();
        if(value == null || value.isEmpty()) {
            return object;
        }
        String[] records = value.split("[,]");
        for(String record : records) {
            String[] parts = record.split("[|]");
            JSONObject recordObj = new JSONObject();
            for(int i = 0; i < partNames.length && i < parts.length; i++) {
                recordObj.put(partNames[i], parts.length > i ? parts[i] : null);
            }
            object.put(parts[0], recordObj);
        }

        node.put(field, object);
        return object;
    }
}
