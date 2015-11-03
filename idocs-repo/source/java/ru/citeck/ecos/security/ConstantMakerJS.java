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
package ru.citeck.ecos.security;

import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;

public class ConstantMakerJS extends BaseScopableProcessorExtension
{
	private Map<String,ConstantMaker> implementations;
	private String implId;
	
	public void makeConstant(ScriptNode node)
	{
		ConstantMaker impl = implementations.get(implId);
		if(impl == null) {
			throw new IllegalStateException("No such constant maker implementation: " + implId);
		}
		impl.makeConstant(node.getNodeRef());
	}

	public void setImplementations(Map<String,ConstantMaker> implementations) {
		this.implementations = implementations;
	}

	public void setImplId(String implId) {
		this.implId = implId;
	}
}
