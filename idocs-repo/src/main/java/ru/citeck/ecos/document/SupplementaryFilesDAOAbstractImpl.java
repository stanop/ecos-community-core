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
package ru.citeck.ecos.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class SupplementaryFilesDAOAbstractImpl implements SupplementaryFilesDAO 
{
	@Override
	public void setSupplementaryFiles(NodeRef document, List<NodeRef> files) {
		
		// get current supplementary files list:
		HashSet<NodeRef> oldFiles = new HashSet<NodeRef>();
		HashSet<NodeRef> newFiles = new HashSet<NodeRef>();
		oldFiles.addAll(this.getSupplementaryFiles(document));
		newFiles.addAll(files);
		
		// get lists of files to add and to remove:
		List<NodeRef> filesToAdd = new ArrayList<NodeRef>(newFiles.size());
		for(NodeRef file : newFiles) {
			if(!oldFiles.contains(file)) {
				filesToAdd.add(file);
			}
		}
		
		List<NodeRef> filesToRemove = new ArrayList<NodeRef>(oldFiles.size());
		for(NodeRef file : oldFiles) {
			if(!newFiles.contains(file)) {
				filesToRemove.add(file);
			}
		}

		// actually add new files and remove old files:
		this.addSupplementaryFiles(document, filesToAdd);
		this.removeSupplementaryFiles(document, filesToRemove);
		
	}
	
}
