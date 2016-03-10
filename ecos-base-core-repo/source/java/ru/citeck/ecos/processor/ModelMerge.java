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
import java.util.Map;

/**
 * Model Merge is a Data Bundle Merge, that merges only models.
 * It puts models into one and returns it in Data Bundle with no input stream.
 * 
 * It can be used by true Merges as an implementation of model merging.
 * 
 * @author Sergey Tiunov
 *
 */
public class ModelMerge extends AbstractDataBundleMerge
{

	@Override
	public DataBundle merge(List<DataBundle> inputs) {
		Map<String,Object> model = DataBundle.emptyModel();
		for(DataBundle input : inputs) {
			Map<String,Object> inputModel = input.getModel();
			if(inputModel != null) {
				model.putAll(inputModel);
			}
		}
		return new DataBundle(model);
	}

}
