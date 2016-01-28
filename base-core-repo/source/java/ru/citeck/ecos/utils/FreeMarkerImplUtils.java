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
package ru.citeck.ecos.utils;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;

public class FreeMarkerImplUtils {

    public static TemplateNode wrapNode(NodeRef nodeRef, 
            AlfrescoTemplateProcessorExtension scopeProvider) 
    {
        if(nodeRef == null) {
            return null;
        }
        return scopeProvider.createNode(nodeRef);
    }
    
    public static List<TemplateNode> wrapNodes(List<NodeRef> nodeRefs, 
            AlfrescoTemplateProcessorExtension scopeProvider) 
    {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>(nodeRefs.size());
        for(NodeRef nodeRef : nodeRefs) {
            nodes.add(wrapNode(nodeRef, scopeProvider));
        }
        return nodes;
    }
    
    public static TemplateNode wrapGroup(String groupName, 
            AlfrescoTemplateProcessorExtension scopeProvider) 
    {
        if(groupName == null) {
            return null;
        }
        NodeRef groupNodeRef = scopeProvider.getServiceRegistry().getAuthorityService().getAuthorityNodeRef(groupName);
        return scopeProvider.createNode(groupNodeRef);
    }
    
    public static List<TemplateNode> wrapGroups(List<String> groupNames, 
            AlfrescoTemplateProcessorExtension scopeProvider) 
    {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>(groupNames.size());
        for(String groupName : groupNames) {
            nodes.add(wrapGroup(groupName, scopeProvider));
        }
        return nodes;
    }
    
    public static TemplateNode wrapUser(String userName, 
            AlfrescoTemplateProcessorExtension scopeProvider) 
    {
        if(userName == null) {
            return null;
        }
        NodeRef userNodeRef = scopeProvider.getServiceRegistry().getPersonService().getPerson(userName);
        return scopeProvider.createNode(userNodeRef);
    }
    
    public static List<TemplateNode> wrapUsers(List<String> userNames, 
            AlfrescoTemplateProcessorExtension scopeProvider) 
    {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>(userNames.size());
        for(String userName : userNames) {
            nodes.add(wrapUser(userName, scopeProvider));
        }
        return nodes;
    }
    
    
}
