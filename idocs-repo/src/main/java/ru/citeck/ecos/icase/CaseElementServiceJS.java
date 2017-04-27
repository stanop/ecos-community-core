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
package ru.citeck.ecos.icase;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.model.ICaseTemplateModel;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;
import ru.citeck.ecos.utils.RepoUtils;

/**
 * This implementation is used in JS side.
 * 
 * @author Ruslan Vildanov
 *
 */
public class CaseElementServiceJS extends AlfrescoScopableProcessorExtension {

    private static final Log log = LogFactory.getLog(CaseElementServiceJS.class);

    private CaseElementService caseElementService;

    public void setCaseElementService(CaseElementService caseElementService) {
        this.caseElementService = caseElementService;
    }
    
    public ScriptNode[] getElements(Object caseNodeRef, String configName) {
        NodeRef caseNr = getNodeRef(caseNodeRef);
        List<NodeRef> elements = caseElementService.getElements(caseNr, configName);
        return JavaScriptImplUtils.wrapNodes(elements, this);
    }

    public ScriptNode[] getCases(Object nodeRef, String configName) {
        NodeRef nr = getNodeRef(nodeRef);
        List<NodeRef> cases = caseElementService.getCases(nr, configName);
        return JavaScriptImplUtils.wrapNodes(cases, this);
    }

    public void addElement(Object nodeRef, Object caseNodeRef, String configName)
            throws AlfrescoRuntimeException {
        NodeRef nr = getNodeRef(nodeRef);
        NodeRef caseNr = getNodeRef(caseNodeRef);
        caseElementService.addElement(nr, caseNr, configName);
    }

    public void removeElement(Object nodeRef, Object caseNodeRef, String configName)
             throws AlfrescoRuntimeException {
        NodeRef nr = getNodeRef(nodeRef);
        NodeRef caseNr = getNodeRef(caseNodeRef);
        caseElementService.removeElement(nr, caseNr, configName);
    }

    public ScriptNode destination(Object caseNodeRef, String configName)
            throws AlfrescoRuntimeException {
        NodeRef caseNr = getNodeRef(caseNodeRef);
        NodeRef destNr = caseElementService.destination(caseNr, configName);
        return JavaScriptImplUtils.wrapNode(destNr, this);
    }
    
    public void copyCaseToTemplate(Object caseNode, Object template) {
        NodeRef caseNodeRef = getNodeRef(caseNode);
        NodeRef templateRef = getNodeRef(template);
        caseElementService.copyCaseToTemplate(caseNodeRef, templateRef);
    }

    public void copyTemplateToCase(Object template, Object caseNode) {
        NodeRef templateRef = getNodeRef(template);
        NodeRef caseNodeRef = getNodeRef(caseNode);
        caseElementService.copyTemplateToCase(templateRef, caseNodeRef);
    }

    private NodeRef getNodeRef(Object object) {
        if(object == null)
            return null;
        if(object instanceof NodeRef) 
            return (NodeRef) object;
        if(object instanceof String) 
            return new NodeRef((String) object);
        if(object instanceof ScriptNode)
            return ((ScriptNode) object).getNodeRef();
        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to NodeRef");
    }
    
}
