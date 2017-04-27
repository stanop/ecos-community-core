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
import java.util.List;

/**
 * Abstract Data Bundle Line.
 * Implements "many-to-many" process in terms of "one-to-one" process.
 * 
 * @author Sergey Tiunov
 *
 */
public abstract class AbstractDataBundleLine extends AbstractDataBundleProcessor implements DataBundleLine 
{

	@Override
	public List<DataBundle> process(List<DataBundle> inputs) {
		List<DataBundle> outputs = new ArrayList<DataBundle>(inputs.size());
		for(DataBundle input : inputs) {
			DataBundle output = process(input);
			if(output != null) {
				outputs.add(output);
			}
		}
		return outputs;
	}

}
