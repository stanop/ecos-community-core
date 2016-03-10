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
 * Sequential Data Bundle Processor is a type of Composite Data Bundle Processor.
 * It uses other processors to process input Data Bundles "sequentially".
 * 
 * "Sequentially" means the following:
 * All inputs are first processed by first processor, its outputs are presented to second, 
 * second outputs - to third, and so on.
 * Output of sequential processor is output of the last processor in sequence.
 * 
 * Sequential processors can be widely used to act like an assembly line:
 * processors are connected in sequence.
 * 
 * @author Sergey Tiunov
 *
 */
public class SequentialDataBundleProcessor extends AbstractCompositeDataBundleProcessor
{

	@Override
	public List<DataBundle> process(List<DataBundle> inputs) {
		List<DataBundle> outputs = null;
		for(DataBundleProcessor processor : processors) {
			outputs = processor.process(inputs);
			inputs = outputs;
		}
		return outputs;
	}

}
