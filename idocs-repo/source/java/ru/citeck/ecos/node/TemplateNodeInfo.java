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
package ru.citeck.ecos.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.template.PropertyConverter;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;

public class TemplateNodeInfo implements NamespacePrefixResolverProvider
{
	private static final long serialVersionUID = -2633969468555475929L;
	
	private ServiceRegistry services;
	private TemplateImageResolver imageResolver;
	
	private Map<String,Serializable> properties;
	private Map<String,List<TemplateNode>> targetAssocs;
	private Map<String,List<TemplateNode>> childAssocs;
	
	private PropertyConverter propertyConverter = new PropertyConverter();
    
	public TemplateNodeInfo(ServiceRegistry services, TemplateImageResolver imageResolver) {
		this.services = services;
		this.imageResolver = imageResolver;
	}
	
    public TemplateNodeInfo(NodeInfo nodeInfo, ServiceRegistry services, TemplateImageResolver imageResolver) {
    	this(services, imageResolver);
    	
    	// set information:
    	this.setProperties(nodeInfo.getProperties());
    	this.setTargetAssocs(nodeInfo.getTargetAssocs());
    	this.setChildAssocs(nodeInfo.getChildAssocs());
    }
    
	@Override
	public NamespacePrefixResolver getNamespacePrefixResolver() {
		return this.services.getNamespaceService();
	}
	
	public Map<String,Serializable> getProperties() {
		return properties;
	}
	
	public Map<String,List<TemplateNode>> getAssocs() {
		return targetAssocs;
	}
	
	public Map<String,List<TemplateNode>> getAssociations() {
		return getAssocs();
	}
	
	public Map<String,List<TemplateNode>> getChildAssocs() {
		return childAssocs;
	}
	
	@SuppressWarnings("unchecked")
	public void setProperties(Map<QName,Serializable> props) {
		if(props == null) {
			this.properties = null;
			return;
		}
		this.properties = new QNameMap<String,Serializable>(this);
        for (QName qname : props.keySet())
        {
            Serializable value = this.propertyConverter.convertProperty(
                    props.get(qname), qname, this.services, imageResolver);
            this.properties.put(qname.toString(), value);
        }
	}
	
	public void setTargetAssocs(Map<QName,List<NodeRef>> assocs) {
		this.targetAssocs = this.convertAssocMap(assocs);
	}
	
	public void setChildAssocs(Map<QName,List<NodeRef>> assocs) {
		this.childAssocs = this.convertAssocMap(assocs);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,List<TemplateNode>> convertAssocMap(Map<QName,List<NodeRef>> assocMap) {
		if(assocMap == null) {
			return null;
		}
		Map<String,List<TemplateNode>> templateAssocMap = new QNameMap<String,List<TemplateNode>>(this);
		for (QName qname : assocMap.keySet())
		{
			List<NodeRef> nodes = assocMap.get(qname);
			List<TemplateNode> templateNodes = new ArrayList<TemplateNode>(nodes.size());
			for(NodeRef node : nodes) {
				templateNodes.add(new TemplateNode(node, services, imageResolver));
			}
			templateAssocMap.put(qname.toString(), templateNodes);
		}
		return templateAssocMap;
	}
}
