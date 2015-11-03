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

import org.alfresco.service.cmr.repository.NodeRef;

import ru.citeck.ecos.document.SupplementaryFilesDAO;

/**
 * Data Bundle Split, that splits Data Bundles based on supplementary files of some node.
 * 
 * @author Sergey Tiunov
 *
 */
public class SupplementaryFilesSplit extends AbstractAssociationSplit
{
	
	private SupplementaryFilesDAO dao;

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List<Object> getAssociatedObjects(NodeRef nodeRef) {
		List supplementaryFiles = dao.getSupplementaryFiles(nodeRef);
		return supplementaryFiles;
	}

	public void setDao(SupplementaryFilesDAO dao) {
		this.dao = dao;
	}
	
	

}
