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
package ru.citeck.ecos.processor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Parses input as JSON and stores it in specified key in model.
 * If key is not specified, the parsed object populates the model root.
 * 
 * @author Sergey Tiunov
 */
public class ParseJSON extends AbstractDataBundleLine {

    private String modelKey;
    
    @Override
    public DataBundle process(DataBundle input) {
        
        Map<String, Object> model = input.getModel();
        
        Object result = null;
        try {
            if(model == null || !model.containsKey(ProcessorConstants.KEY_ENCODING)) {
                result = JSONValue.parseWithException(new InputStreamReader(input.needInputStream()));
            } else {
                result = JSONValue.parseWithException(new InputStreamReader(
                        input.needInputStream(), (String) model.get(ProcessorConstants.KEY_ENCODING)));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse input data", e);
        }
        
        Map<String, Object> newModel = model != null ? 
                new HashMap<String, Object>(model) : 
                new HashMap<String, Object>();
        if(modelKey != null) {
            newModel.put(modelKey, result);
        } else if(result instanceof Map) {
            Map<?,?> object = (Map<?,?>) result;
            for(Object key : object.keySet()) {
                if(key instanceof String) {
                    newModel.put((String) key, object.get(key));
                } else if(key != null) {
                    newModel.put(key.toString(), object.get(key));
                }
            }
        } else {
            throw new IllegalArgumentException("Can not populate model with " + result + " as it is not Map");
        }
        
        return new DataBundle(input, newModel);
    }

    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }

}
