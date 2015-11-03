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
package ru.citeck.ecos.counter;

import java.util.Map;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JSONUtils;

import static ru.citeck.ecos.utils.JavaScriptImplUtils.*;

public class EnumerationServiceJS extends AlfrescoScopableProcessorExtension {

    private EnumerationService enumerationService;
    
    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public ScriptNode getTemplate(String templateName) {
        NodeRef template;
        if(NodeRef.isNodeRef(templateName)) {
            template = new NodeRef(templateName);
            if(!enumerationService.isTemplate(template)) return null;
        } else {
            template = enumerationService.getTemplate(templateName);
        }
        return template != null ? wrapNode(template, this) : null;
    }
    
    public boolean isTemplate(ScriptNode node) {
        return enumerationService.isTemplate(node.getNodeRef());
    }
    
    public String getNumber(ScriptNode template, ScriptNode node) throws EnumerationException {
        return enumerationService.getNumber(template.getNodeRef(), node.getNodeRef());
        
    }
    
    public String getNumber(String templateName, ScriptNode node) throws EnumerationException {
        return enumerationService.getNumber(
                needTemplate(templateName), 
                node.getNodeRef());
    }

    private NodeRef needTemplate(String templateName) {
        NodeRef template = enumerationService.getTemplate(templateName);
        if(template == null) {
            throw new IllegalArgumentException("Can not find template " + templateName);
        }
        return template;
    }
    
    public String getNumber(ScriptNode template, ScriptableObject model) throws EnumerationException {
        return enumerationService.getNumber(
                template.getNodeRef(),
                convertScriptableToMap(model));
    }
    
    public String getNumber(String templateName, ScriptableObject model) throws EnumerationException {
        return enumerationService.getNumber(
                needTemplate(templateName),
                convertScriptableToMap(model));
    }
    
    public String getNumber(ScriptNode template, Map<String, Object> model) throws EnumerationException {
        return enumerationService.getNumber(template.getNodeRef(), model);
    }
    
    public String getNumber(String templateName, Map<String, Object> model) throws EnumerationException {
        return enumerationService.getNumber(needTemplate(templateName), model);
    }
    
    public String getNumber(ScriptNode template, JSONObject model) throws EnumerationException {
        return enumerationService.getNumber(
                template.getNodeRef(),
                JSONUtils.convertJSON(model));
    }
    
    public String getNumber(String templateName, JSONObject model) throws EnumerationException {
        return enumerationService.getNumber(
                needTemplate(templateName),
                JSONUtils.convertJSON(model));
    }
    
}
