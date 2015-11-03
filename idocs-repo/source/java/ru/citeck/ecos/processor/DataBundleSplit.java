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
 * Data Bundle Split is a Data Bundle Processor, that splits each input Data Bundle into several (0..*) output Data Bundles.
 * For that purpose Split has "one-to-many" process method.
 * 
 * The split is performed by the criteria, that depend on implementation. 
 * In fact, these criteria identify the split.
 * 
 * No special constraints are set on inputs, but they may be set by specific implementations.
 * 
 * @author Sergey Tiunov
 *
 */
public interface DataBundleSplit extends DataBundleProcessor {

	/**
	 * Split input Data Bundle into list of Data Bundles.
	 * 
	 * @param input - input Data Bundle
	 * @return list of output Data Bundles
	 */
	List<DataBundle> split(DataBundle input);
	
}
