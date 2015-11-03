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

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class TypeFormProcessor extends org.alfresco.repo.forms.processor.node.TypeFormProcessor 
{
	private FormNodeBuilderRegistry builderRegistry;

	public void setBuilderRegistry(FormNodeBuilderRegistry builderRegistry) {
		this.builderRegistry = builderRegistry;
	}
	
	@Override
	protected NodeRef createNode(TypeDefinition typeDef, FormData data) {
		FormNodeBuilder builder = builderRegistry.getNodeBuilder(typeDef);
		// if no builder found - do not return anything
		if(builder == null) {
			if(getLogger().isWarnEnabled()) {
				getLogger().warn("Can't find FormNodeBuilder for type " + typeDef.getName().toPrefixString());
			}
			return null;
		}
		return builder.createNode(typeDef, data);
	}
	
	@Override
    protected Set<QName> getAspectNames(TypeDefinition typeDef)
    {
        Set<QName> aspectNames = new HashSet<QName>();
        getAspectNames(typeDef, aspectNames);
        return aspectNames;
    }
    
    private void getAspectNames(ClassDefinition classDef, Set<QName> aspectNames) {
        for(ClassDefinition aspectDef : classDef.getDefaultAspects()) {
            aspectNames.add(aspectDef.getName());
            getAspectNames(aspectDef, aspectNames);
        }
    }
    
}
