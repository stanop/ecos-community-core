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
 * Data Bundle Merge is a Data Bundle Processor, that can merge several input Data Bundles into one Data Bundle.
 * If no inputs are given, no outputs would be given.
 * For that purpose Merge has "many-to-one" process method.
 * 
 * Usually both input stream and model should be merged.
 * The algorithms of merging identify different implementations.
 * 
 * @author Sergey Tiunov
 *
 */
public interface DataBundleMerge extends DataBundleProcessor {

	/**
	 * Merge several Data Bundles into one Data Bundle.
	 * 
	 * @param inputs - Input Data Bundles to merge
	 * @return output Data Bundle, or null, if no inputs are given
	 */
	public DataBundle merge(List<DataBundle> inputs);
	
}
