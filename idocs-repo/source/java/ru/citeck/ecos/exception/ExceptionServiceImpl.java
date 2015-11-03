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
package ru.citeck.ecos.exception;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public class ExceptionServiceImpl implements ExceptionService {
	protected NodeService nodeService;

	@Override
	public ExceptionTranslator getExceptionTranslator(NodeRef nodeRef, QName config) {
		ExceptionTranslator result = null;
		if (nodeService.exists(nodeRef)) {
			Serializable s = nodeService.getProperty(nodeRef, config);
			if (s instanceof String)
				result = getExceptionTranslator((String)s);
		}
		return result == null ? getExceptionTranslator("") : result;
	}

	@Override
	public ExceptionTranslator getExceptionTranslator(String config) {
		return new ExceptionTranslatorImpl(config);
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

}
