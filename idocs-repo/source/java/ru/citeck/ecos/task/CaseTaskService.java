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
package ru.citeck.ecos.task;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;
import java.util.Map;

/**
 * Created by maxim.strizhov on 13.10.2015.
 */
public interface CaseTaskService {
    List<NodeRef> getTasks(NodeRef nodeRef);
    void startTask(NodeRef taskNodeRef);
    void onTaskCompleted(NodeRef nodeRef, String processType, String processInstanceId);
}
