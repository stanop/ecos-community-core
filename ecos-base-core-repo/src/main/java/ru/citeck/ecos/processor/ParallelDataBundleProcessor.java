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
 * Parallel Data Bundle Processor is a type of Composite Data Bundle Processor.
 * It uses other processors to process input Data Bundles "in parallel".
 * 
 * "In parallel" means the following.
 * All inputs are first processed by first processor (its outputs go first), 
 * then - by second processor (its outputs go second) and so on.
 * Output of parallel processor is a concatenated list of outputs of all processors.
 * 
 * Parallel processor can be used to concatenate different types of processing.
 * E.g. first processor outputs document metadata, and second one prints its content.
 * 
 * To process the inputs several times (Data Bundle input stream can be read just once), 
 * inputs are backed up first and all the processors are presented with a fresh input stream of the backup.
 * 
 * @author Sergey Tiunov
 *
 */
public class ParallelDataBundleProcessor extends AbstractCompositeDataBundleProcessor
{
	@Override
	public List<DataBundle> process(List<DataBundle> inputs) {
		// backup input streams, because they will be opened many times
		List<DataBundle> backups = new ArrayList<DataBundle>(inputs.size());
		for(DataBundle input : inputs) {
			DataBundle backup = helper.backupDataBundle(input);
			if(backup != null) {
				backups.add(backup);
			}
		}

		// process backups instead of inputs
		List<DataBundle> outputs = new ArrayList<DataBundle>(processors.size());
		for(DataBundleProcessor processor : processors) {
			outputs.addAll(processor.process(backups));
		}
		return outputs;
	}
	
}
