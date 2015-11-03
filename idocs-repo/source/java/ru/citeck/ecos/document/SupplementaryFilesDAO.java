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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Data Access Object interface for supplementary files of document.
 * Has methods to get, set, add and remove supplementary files.
 * 
 * @author Sergey Tiunov
 */
public interface SupplementaryFilesDAO {

	/**
	 * Get supplementary files list for specified document.
	 * @param document
	 * @return
	 */
	public List<NodeRef> getSupplementaryFiles(NodeRef document);
	
	/**
	 * Set supplementary files for specified document.
	 * Old supplementary files are removed, and new files are added.
	 * @param document
	 * @param files
	 */
	public void setSupplementaryFiles(NodeRef document, List<NodeRef> files);
	
	/**
	 * Add supplementary files to specified document.
	 * @param document
	 * @param files
	 */
	public void addSupplementaryFiles(NodeRef document, List<NodeRef> files);
	
	/**
	 * Remove supplementary files from specified document.
	 * @param document
	 * @param files
	 */
	public void removeSupplementaryFiles(NodeRef document, List<NodeRef> files);

	/**
	 * Get parent files list for specified document.
	 * @param document
	 * @return list of parent files noderefs
	 */
	public List<NodeRef> getParentFiles(NodeRef document);
}
