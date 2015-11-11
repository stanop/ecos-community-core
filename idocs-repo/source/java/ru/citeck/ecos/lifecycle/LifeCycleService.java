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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleState;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;

/**
 * @author: Alexander Nemerov
 * @date: 18.02.14
 */
public interface LifeCycleService extends LifeCycleServiceGeneric<NodeRef,
        LifeCycleTransition, List<LifeCycleTransition>, List<NodeRef>, LifeCycleState> {

    boolean doTransitionOnStartProcess(NodeRef nodeRef, String processType, Map<String, Object> model);

    boolean doTransitionOnEndProcess(NodeRef nodeRef, String processType, Map<String, Object> model);

    boolean doTransitionOnSignal(NodeRef nodeRef, String signalId, Map<String, Object> model);

    boolean doTimerTransition(NodeRef nodeRef);

    LifeCycleDefinition getLifeCycleDefinitionByDocType(QName docType);

    void deployLifeCycle(InputStream lifeCycleDefinitionStream, String formatName, QName docType, String title) throws IOException;

    void undeployLifeCycle(QName docType);

    // TODO refactor stored lifecycles!
    
    void deployStoredLifeCycle(NodeRef lifeCycleDefinitionNodeRef);

    void deployStoredLifeCycles();

    void undeployStoredLifeCycle(NodeRef lifeCycleDefinitionNodeRef);

    List<NodeRef> getStoredLifeCycleDefinitions();

    List<Map<String, Object>> getStoredLifeCycleDefinitionsHeaders(QName requiredDocType);

    List<NodeRef> getStoredLifeCycleDefinitionsByDocType(QName docType);

    LifeCycleDefinition getStoredLifeCycleDefinition(NodeRef nodeRef);

    boolean isNodeWithLifeCycleDefinition(NodeRef nodeRef);

    NodeRef storeLifeCycleDefinition(NodeRef nodeRef, String content, String formatName, QName docType, String title, Boolean enabled);

    Set<QName> getDocumentTypesWithLifeCycleDefinitions();

    String serializeLifeCycleDefinition(LifeCycleDefinition lcd, String format);
}
