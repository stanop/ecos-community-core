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
package ru.citeck.ecos.lifecycle;

import java.util.List;

import org.alfresco.repo.jscript.ScriptNode;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleState;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;

/**
 * @author: Alexander Nemerov
 * @date: 21.04.2014
 */
public interface LifeCycleServiceJS extends LifeCycleServiceGeneric<String, 
		LifeCycleTransition, List<LifeCycleTransition>, ScriptNode[], LifeCycleState> {

}
