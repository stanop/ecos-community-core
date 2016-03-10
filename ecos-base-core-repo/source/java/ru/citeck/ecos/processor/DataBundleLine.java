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

/**
 * Data Bundle Line - is a linear Data Bundle Processor.
 * Each input of Line should be mapped to one corresponding output.
 * For that purpose Line has "one-to-one" process method.
 * 
 * There can be many types of Lines:
 * - generators - generate data bundle from scratch
 * - transformers - transform data bundle into another format
 * - decorators - decorate data bundle in the same format
 * - etc.
 * 
 * Each type of Line may state different constraints on input Data Bundle,
 * e.g. generators do not need input Data Bundle (or its input stream, at least), 
 * whereas transformers do.
 * 
 * @author Sergey Tiunov
 *
 */
public interface DataBundleLine extends DataBundleProcessor {

	/**
	 * Process one input Data Bundle and generate one output Data Bundle.
	 * 
	 * @param input - Input Data Bundle
	 * @return Output Data Bundle, or null
	 */
	public DataBundle process(DataBundle input);
	
}
