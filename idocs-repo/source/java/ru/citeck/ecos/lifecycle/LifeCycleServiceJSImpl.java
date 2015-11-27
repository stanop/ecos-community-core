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

import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapNode;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapNodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptNode.ScriptContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleState;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author: Alexander Nemerov
 * @date: 06.03.14
 */
public class LifeCycleServiceJSImpl extends AlfrescoScopableProcessorExtension
        implements LifeCycleServiceJS {

    private LifeCycleService lifeCycleService;

    private NamespaceService namespaceService;

    @Override
    public String getDocumentState(String nodeRef) {
        return lifeCycleService.getDocumentState(new NodeRef(nodeRef));
    }

    @Override
    public List<LifeCycleTransition> getAvailableUserEvents(String nodeRef) {
        return lifeCycleService.getAvailableUserEvents(new NodeRef(nodeRef));
    }

    @Override
    public boolean doTransition(String nodeRef, String transition) {
        if (NodeRef.isNodeRef(transition)) {
        	//TODO:
            /*return lifeCycleService.doTransition(new NodeRef(nodeRef),
                    new NodeRef(transition));*/
        	return false;
        } else {
            return lifeCycleService.doTransition(new NodeRef(nodeRef), transition);
        }
    }

    @Override
    public boolean doTransition(String nodeRef, LifeCycleTransition transition, LifeCycleState fromStateDef, LifeCycleState toStateDef) {
        return lifeCycleService.doTransition(new NodeRef(nodeRef), transition, fromStateDef, toStateDef);
    }

    @Override
    public List<LifeCycleTransition> getTransitionsByDocState(String nodeRef) {
        return lifeCycleService.getTransitionsByDocState(new NodeRef(nodeRef));
    }

    @Override
    public ScriptNode[] getDocumentsWithTimer() {
        return wrapNodes(lifeCycleService.getDocumentsWithTimer(), this);
    }

    public void createTableFromFile(String nodeRef, String type, String format) throws IOException {
        QName docType = QName.resolveToQName(namespaceService, type);
        ScriptNode node = wrapNode(nodeRef, this);
        ScriptContentData contentData = (ScriptContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
        if(contentData == null) {
            throw new IllegalArgumentException("Node " + nodeRef + " does not have content");
        }
        InputStream inputStream = contentData.getInputStream();
        String formatName = LifeCycleCSVFormat.NAME;
        if (format.equalsIgnoreCase("xml")) {
            formatName = LifeCycleXMLFormat.NAME;
        }
        lifeCycleService.deployLifeCycle(inputStream, formatName, docType, format);
    }

    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
