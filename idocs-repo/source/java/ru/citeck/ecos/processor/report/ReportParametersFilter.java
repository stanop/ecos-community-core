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
package ru.citeck.ecos.processor.report;

import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.ProcessorConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Modify and pass report parameters to output model
 *
 * @author Alexey Moiseev <alexey.moiseev@citeck.ru>
 */
public class ReportParametersFilter extends AbstractDataBundleLine {

    @Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = input.needModel();
        
        InputStream is = input.needInputStream();
        ByteArrayOutputStream os = copyInputStream(is);
        InputStream copyIS = new ByteArrayInputStream(os.toByteArray());
        InputStream newIS = new ByteArrayInputStream(os.toByteArray());
        
        try {
			os.close();
		} catch (IOException e) {
			Logger.getLogger(ReportParametersFilter.class).error(e.getMessage(), e);
		}
        
        DataBundle copyDB = new DataBundle(copyIS, model);
        
        ContentReader contentReader = helper.getContentReader(copyDB);
        Object criteriaObj = evaluateExpression(contentReader.getContentString(), model);
                
        HashMap<String, Object> newModel = new HashMap<String, Object>();
        newModel.putAll(model);
        newModel = insertReportParams(criteriaObj, newModel);
        
        return new DataBundle(newIS, newModel);
    }
    
    private HashMap<String, Object> insertReportParams(Object criteriaObj, HashMap<String, Object> model) {
    	JSONObject criteriaJSON = null;
    	
    	if (criteriaObj instanceof String) {
            try {
            	criteriaJSON = new JSONObject((String) criteriaObj);
            } catch (JSONException e) {
            }
        } else if (criteriaObj instanceof JSONObject) {
        	criteriaJSON = (JSONObject) criteriaObj;
        }
    	
    	if (criteriaJSON != null) {
			Iterator criteriaKeys = criteriaJSON.sortedKeys();
	        while (criteriaKeys.hasNext()) {
	            String name = (String) criteriaKeys.next();
	            if (name.startsWith("report")) {
	                try {
	                	if (name.equals("reportFilename"))
	                		model.put(ProcessorConstants.KEY_FILENAME, simpleJSON2Java(criteriaJSON.get(name)));
	                	model.put(name, simpleJSON2Java(criteriaJSON.get(name)));
	                } catch (JSONException e) {
	                }
	            }
	        }
    	}
    	
    	return model;
    }
    
    private Object simpleJSON2Java(Object o) {
    	if (o != null) {
    		if (o instanceof JSONArray) {
    			JSONArray arr = (JSONArray) o;
    			List<Object> list = new ArrayList<Object>();
    			
    			for (int i = 0; i < arr.length(); i++) {
					try {
						list.add(simpleJSON2Java(arr.get(i)));
					} catch (JSONException e) {
					}
    			}
    			
    			return list;
    		} else if (o instanceof JSONObject) {
    			JSONObject obj = (JSONObject) o;
    			Map<String, Object> map = new HashMap<String, Object>(); 
    			Iterator keys = obj.sortedKeys();
    			
    	        while (keys.hasNext()) {
    	            String name = (String) keys.next();
    	            try {
						map.put(name, simpleJSON2Java(obj.get(name)));
					} catch (JSONException e) {
					}
    	        }
    	        
    	        return map;
    		} else
    			return o;
    	}
    	
    	return null;
    }
    
    private ByteArrayOutputStream copyInputStream(InputStream is) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	try {
    		IOUtils.copy(is, baos);
    	} catch (IOException e) {
    		Logger.getLogger(ReportParametersFilter.class).error("Cannot copy input stream", e);
    	}
    	
    	return baos;
    }
}