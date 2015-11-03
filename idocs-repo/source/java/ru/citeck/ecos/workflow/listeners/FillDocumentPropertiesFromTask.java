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

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.AssociationRef;

import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.namespace.QName;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * This task listener fill document properties from task
 * 
 * @author Elena Zaripova
 */
public class FillDocumentPropertiesFromTask extends AbstractTaskListener 
{

    private NodeService nodeService;
    private WorkflowQNameConverter qNameConverter;
    private Map<String,String> propertiesList;
    private Map<String,String> assocsList;
    private Map<String,String> childAssocsList;
    private String taskName;
	
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.qNameConverter = new WorkflowQNameConverter(serviceRegistry.getNamespaceService());
    }

    @Override
    protected void notifyImpl(final DelegateTask task) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
				final WorkflowTask wfTask = serviceRegistry.getWorkflowService().getTaskById("activiti$"+task.getId());
				if(wfTask!=null && wfTask.getName()!=null && wfTask.getName().equals(taskName))
				{
					NodeRef docRef = ListenerUtils.getDocument(task.getExecution(), nodeService);
					if(nodeService.exists(docRef))
					{
						if(propertiesList!=null)
						{
						for(Map.Entry<String, String> entry : propertiesList.entrySet())
						{
							QName propNameInTask = qNameConverter.mapNameToQName(entry.getKey());
							QName propNameInDoc = qNameConverter.mapNameToQName(entry.getValue());
							if(wfTask.getProperties().get(propNameInTask)!=null)
								nodeService.setProperty(docRef, propNameInDoc, wfTask.getProperties().get(propNameInTask));
						}
						}
						if(assocsList!=null)
						{
						for(Map.Entry<String, String> entry : assocsList.entrySet())
						{
							QName assocNameInTask = qNameConverter.mapNameToQName(entry.getKey());
							QName assocNameInDoc = qNameConverter.mapNameToQName(entry.getValue());
							List<AssociationRef> assocs = nodeService.getTargetAssocs(docRef, assocNameInDoc);
							for(AssociationRef existingAssoc : assocs)
							{
								NodeRef nodeTarget = existingAssoc.getTargetRef();
								NodeRef nodeSource = existingAssoc.getSourceRef();
								nodeService.removeAssociation(nodeSource, nodeTarget, assocNameInDoc);
							}
							if(wfTask.getProperties().get(assocNameInTask)!=null)
							{
								if (wfTask.getProperties().get(assocNameInTask) instanceof ArrayList) 
								{
									ArrayList<NodeRef> nodes = (ArrayList<NodeRef>)wfTask.getProperties().get(assocNameInTask);
									for (NodeRef node : nodes)
									{
										nodeService.createAssociation(docRef, node, assocNameInDoc);
									}
								}
								else if (wfTask.getProperties().get(assocNameInTask) instanceof NodeRef) 
								{
									nodeService.createAssociation(docRef, (NodeRef)wfTask.getProperties().get(assocNameInTask), assocNameInDoc);
								}
							}
						}
						}
						if(childAssocsList!=null)
						{
						for(Map.Entry<String, String> entry : childAssocsList.entrySet())
						{
							QName childAssocNameInTask = qNameConverter.mapNameToQName(entry.getKey());
							QName childAssocNameInDoc = qNameConverter.mapNameToQName(entry.getValue());
							List<ChildAssociationRef> existingAssocs = nodeService.getChildAssocs(docRef, childAssocNameInDoc, RegexQNamePattern.MATCH_ALL);
							for(ChildAssociationRef existingChild : existingAssocs)
							{
								nodeService.removeChildAssociation(existingChild);
							}
							if(wfTask.getProperties().get(childAssocNameInTask)!=null)
							{
								if (wfTask.getProperties().get(childAssocNameInTask) instanceof ArrayList) 
								{
									ArrayList<NodeRef> nodes = (ArrayList<NodeRef>)wfTask.getProperties().get(childAssocNameInTask);
									for (NodeRef node : nodes)
									{
										nodeService.addChild(docRef, node, childAssocNameInDoc, nodeService.getPrimaryParent(node).getQName());
									}
								}
								else if (wfTask.getProperties().get(childAssocNameInTask) instanceof NodeRef) 
								{
									nodeService.addChild(docRef, (NodeRef)wfTask.getProperties().get(childAssocNameInTask), childAssocNameInDoc, nodeService.getPrimaryParent((NodeRef)wfTask.getProperties().get(childAssocNameInTask)).getQName());
								}
							}
						}
						}
					}
				}
				return null;
			}
		});
    }
    
	public void setPropertiesList(Map<String,String> propertiesList) {
    	this.propertiesList = propertiesList;
    }
    
	public void setAssocsList(Map<String,String> assocsList) {
    	this.assocsList = assocsList;
    }
    
	public void setChildAssocsList(Map<String,String> childAssocsList) {
    	this.childAssocsList = childAssocsList;
    }

	public void setTaskName(String taskName) {
    	this.taskName = taskName;
    }


}
