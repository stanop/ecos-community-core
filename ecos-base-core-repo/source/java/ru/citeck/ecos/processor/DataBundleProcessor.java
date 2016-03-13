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

import java.util.List;

/**
 * Data Bundle Processor - is a basic processing unit.
 * It gets list of data bundles and returns list of data bundles.
 * For this purpose it has "many-to-many" process method.
 * 
 * The processing chains are built from the processors.
 * 
 * @author Sergey Tiunov
 *
 */
public interface DataBundleProcessor {

	/**
	 * Perform the processing.
	 * 
	 * No constraints are put on the numbers of inputs and outputs: 
	 *  in this contract they can be any and do not relate to each other.
 	 * All outputs of the processor should contain input stream, that can be read - i.e. not closed (if any).
	 * 
	 * @param inputs - list of input data bundles
	 * @return list of output data bundles
	 */
	public List<DataBundle> process(List<DataBundle> inputs);
	
}
