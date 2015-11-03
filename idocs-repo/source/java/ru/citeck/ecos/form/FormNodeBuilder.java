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
package ru.citeck.ecos.form;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.TypeFormProcessor;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

public interface FormNodeBuilder 
{

	public static final String DESTINATION = TypeFormProcessor.DESTINATION;
	public static final String ASSOC_TYPE = "alf_assoctype";

	/**
	 * Creates a new instance of the given type.
	 * This is taken from TypeFormProcessor to encapsulate createNode implementation.
	 * 
	 * @param typeDef The type definition of the type to create
	 * @param data The form data
	 * @return NodeRef representing the newly created node
	 */
	public NodeRef createNode(TypeDefinition typeDef, FormData data);

}
