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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Update Model is a Data Bundle Line, that merges existing model with a new model.
 * Expressions are allowed in the values of new model.
 * 
 * @author Sergey Tiunov
 *
 */
public class UpdateModel extends AbstractDataBundleLine
{
	private Map<String,Object> templateModel;

	@Override
	public DataBundle process(DataBundle input) {
		Map<String,Object> inputModel = input.needModel();
		Map<String,Object> newModel = new HashMap<String,Object>(inputModel.size() + templateModel.size());
		newModel.putAll(inputModel);
		newModel.putAll(this.processTemplateMap(templateModel, inputModel));
		return new DataBundle(input, newModel);
	}
	
	// evaluate all strings as expressions
	private Map<String,Object> processTemplateMap(Map<String,Object> templateMap, 
			Map<String,Object> inputModel) 
	{
		Map<String,Object> map = new HashMap<String,Object>(templateMap.size());
		for(String key : templateMap.keySet()) {
			Object template = templateMap.get(key);
			Object value = this.processTemplateObject(template, inputModel);
			map.put(key, value);
		}
		return map;
	}

	// evaluate all strings as expressions
	private Collection<Object> processTemplateCollection(Collection<Object> templateCollection,
			Map<String, Object> inputModel) 
	{
		Collection<Object> collection = new ArrayList<Object>(templateCollection.size());
		for(Object template : templateCollection) {
			Object value = this.processTemplateObject(template, inputModel);
			collection.add(value);
		}
		return collection;
	}
	
	// evaluate all strings as expressions
	@SuppressWarnings("unchecked")
	private Object processTemplateObject(Object template, Map<String,Object> inputModel) {
		Object value = template;
		if(template == null) {
			// do nothing
		} else if(template instanceof String) {
			value = this.evaluateExpression((String) template, inputModel);
		} else if(template instanceof Map) {
			value = this.processTemplateMap((Map) template, inputModel);
		} else if(template instanceof Collection) {
			value = this.processTemplateCollection((Collection) template, inputModel);
		}
		return value;
	}

	/**
	 * Set model with new fields.
	 * 
	 * @param model
	 */
	public void setModel(Map<String,Object> model) {
		this.templateModel = model;
	}

}
