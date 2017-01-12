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
package ru.citeck.ecos.workflow.confirm;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;
import ru.citeck.ecos.workflow.listeners.ListenerUtils;

public class ConfirmDecisionsJS extends AlfrescoScopableProcessorExtension {

	private ConfirmHelper impl;
	
    public void save(Object packageObj,
            Object confirmerRole, String confirmTaskId)
    {
        impl.saveConfirmDecision(getWorkflowPackage(packageObj), 
                getAuthorityName(confirmerRole), confirmTaskId);
    }
    
	public ConfirmDecision get(DelegateExecution execution, String confirmerRole) 
	{
		return impl.getConfirmDecision(execution, getAuthorityName(confirmerRole));
	}
	
    public ConfirmDecision[] list(DelegateExecution execution) {
        List<ConfirmDecision> confirmDecisions = impl.getConfirmDecisions(execution);
        return confirmDecisions.toArray(new ConfirmDecision[2]);
    }
    
    public ConfirmDecision[] list(ScriptNode node) {
        List<ConfirmDecision> confirmDecisions = impl.getLatestConfirmDecisions(node.getNodeRef());
        return confirmDecisions.toArray(new ConfirmDecision[2]);
    }
    
    public ScriptNode[] listConfirmers(ScriptNode node) {
        return JavaScriptImplUtils.wrapAuthoritiesAsNodes(impl.getLatestConfirmers(node.getNodeRef()), this);
    }

    public ScriptNode[] listConfirmers(ScriptNode node, String outcome) {
        return JavaScriptImplUtils.wrapAuthoritiesAsNodes(impl.getLatestConfirmers(node.getNodeRef(), outcome), this);
    }

    private NodeRef getWorkflowPackage(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof NodeRef) {
            return (NodeRef) obj;
        }
        if(obj instanceof ScriptNode) {
            return ((ScriptNode)obj).getNodeRef();
        }
        if(obj instanceof DelegateExecution) {
            return ListenerUtils.getWorkflowPackage((DelegateExecution)obj);
        }
        throw new IllegalArgumentException("Can not get workflow package for input object of class " + obj.getClass());
    }
    
	private String getAuthorityName(String string) {
		if(!NodeRef.isNodeRef(string)) {
			AuthorityService authorityService = serviceRegistry.getAuthorityService();
			if(!authorityService.authorityExists(string)) {
				throw new IllegalArgumentException("Authority does not exist: " + string);
			}
			return string;
		}
		NodeRef nodeRef = new NodeRef(string);
		return getAuthorityName(nodeRef);
	}
	
    private String getAuthorityName(NodeRef nodeRef) {
        NodeService nodeService = serviceRegistry.getNodeService();
        if(!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("Node does not exist: " + nodeRef);
        }           
        QName type = nodeService.getType(nodeRef);
        if(type.equals(ContentModel.TYPE_PERSON)) {
            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        }
        if(type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
        }
        throw new IllegalArgumentException("Node type is neither cm:person, nor cm:authorityContainer, but " + 
                type + " for node " + nodeRef);
    }

    private String getAuthorityName(Object obj) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof NodeRef) {
            return getAuthorityName((NodeRef) obj);
        }
        if(obj instanceof ScriptNode) {
            return getAuthorityName(((ScriptNode)obj).getNodeRef());
        }
        if(obj instanceof String) {
            return getAuthorityName((String) obj);
        }
        throw new IllegalArgumentException("Can not get authority name for input object of class " + obj.getClass());
    }
	
	public void setImpl(ConfirmHelper impl) {
		this.impl = impl;
	}
	
}
