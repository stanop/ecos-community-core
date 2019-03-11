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
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;

public class DocumentSetListener extends AbstractExecutionListener {
    
    private static final String VAR_DOCUMENT = "document";
    
    private WorkflowDocumentResolverRegistry documentResolverRegistry;

    @Override
    protected void initImpl() {
        documentResolverRegistry = getBean(WorkflowDocumentResolverRegistry.BEAN_NAME, WorkflowDocumentResolverRegistry.class);
    }

    @Override
    protected void notifyImpl(DelegateExecution execution) {
        NodeRef document = documentResolverRegistry.getResolver(execution).getDocument(execution);
        if(document != null) {
            execution.setVariable(VAR_DOCUMENT, new ActivitiScriptNode(document, serviceRegistry));
        } else {
            execution.setVariable(VAR_DOCUMENT, null);
        }
    }
    
}
