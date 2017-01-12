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
package ru.citeck.ecos.archive;

import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class ArchiveServiceJSImpl extends AlfrescoScopableProcessorExtension implements ArchiveServiceJS {

	private static final Log log = LogFactory.getLog(ArchiveServiceJSImpl.class);

	private ArchiveService archiveService;

	@Override
	public void move(String nodeRef, String cause)
			throws InvalidNodeRefException, CyclicChildRelationshipException {

		if (nodeRef instanceof String) {
			NodeRef nr = new NodeRef((String)nodeRef);
			archiveService.move(nr, cause);
		}
		else {
			if (log.isErrorEnabled())
				log.error("Archive service move method has got nodeRef class=" + nodeRef.getClass());
		}
	}

	@Override
	public void clearCache() {
		archiveService.clearCache();
	}

	public void setArchiveService(ArchiveService archiveService) {
		this.archiveService = archiveService;
	}

}
