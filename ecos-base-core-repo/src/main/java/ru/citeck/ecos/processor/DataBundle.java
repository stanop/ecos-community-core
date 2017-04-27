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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Bundle represents some data unit that is processed.
 * Data Bundle consists of content (inputStream) and metadata (model).
 * Data Bundle is constant: neither inputStream, nor model can be changed.
 * Input Stream can be null (no content), and model can be empty (no metadata).
 * 
 * @author Sergey Tiunov
 *
 */
public class DataBundle {

	private InputStream inputStream;
	private Map<String,Object> model;
	
	protected DataBundle() {
		// leave init to child class constructor
	}
	
	/**
	 * Create Data Bundle from input stream (empty model).
	 * @param inputStream
	 */
	public DataBundle(InputStream inputStream) {
		init(inputStream, null);
	}
	
	/**
	 * Create Data Bundle from model (empty input stream).
	 * @param model
	 */
	public DataBundle(Map<String,Object> model) {
		init(null, model);
	}
	
	/**
	 * Create Data Bundle from input stream and model.
	 * @param inputStream
	 * @param model
	 */
	public DataBundle(InputStream inputStream, Map<String,Object> model) {
		init(inputStream, model);
	}

	/**
	 * Create Data Bundle from other Data Bundle and a new input stream.
	 * @param that
	 * @param inputStream
	 */
	public DataBundle(DataBundle that, InputStream inputStream) {
		init(inputStream, that.model);
	}
	
	/**
	 * Create Data Bundle from other Data Bundle and a new model.
	 * @param that
	 * @param model
	 */
	public DataBundle(DataBundle that, Map<String,Object> model) {
		init(that.inputStream, model);
	}
	
//	public DataBundle(DataBundle that, InputStream inputStream, Map<String,Object> model) {
//		Map<String,Object> myModel = new HashMap<String,Object>(that.model.size() + model.size());
//		myModel.putAll(that.model);
//		if(model != null) {
//			myModel.putAll(model);
//		}
//		init(inputStream, myModel);
//	}
//	
	protected void init(InputStream inputStream, Map<String,Object> model) {
		this.inputStream = inputStream;
		// model is unmodifiable
		this.model = Collections.unmodifiableMap(model);
	}

	/**
	 * Get input stream of Data Bundle's content.
	 * If there is no input stream, return null.
	 * @return
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Get input stream of Data Bundle's content.
	 * If there is no input stream, throw exception.
	 * @return
	 */
	public InputStream needInputStream() {
		if(inputStream == null) {
			throw new IllegalStateException("Caller needs input stream, but there is no one");
		}
		return inputStream;
	}
	
	/**
	 * Get model of Data Bundle.
	 * If there is no model, return null.
	 * @return
	 */
	public Map<String,Object> getModel() {
		return model;
	}

	/**
	 * Get model of Data Bundle.
	 * If there is no model, throw exception.
	 * @return
	 */
	public Map<String,Object> needModel() {
		if(model == null) {
			throw new IllegalStateException("Caller needs model, but there is no one");
		}
		return model;
	}
	
	static Map<String,Object> emptyModel() {
		return new HashMap<String,Object>();
	}
}
