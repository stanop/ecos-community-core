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
package ru.citeck.ecos.behavior.common.documentlibrary;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.util.Pair;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;

/**
 * CopyDocumentIgnoreFieldBehaviour behaviour.
 * Skip properties which set in bean property ignoredFields while document copy.
 *
 * @author Elena Zaripova
 */
 
public class CopyDocumentIgnoreFieldBehaviour extends DefaultCopyBehaviourCallback implements CopyServicePolicies.OnCopyNodePolicy {

	protected List<String> ignoredFields;
	protected List<String> ignorePropertiesOnWorkingCopy;
	protected List<String> ignoredAssociations;
	protected NamespaceService namespaceService;
	protected WorkflowQNameConverter qNameConverter;
	protected PolicyComponent policyComponent;
	private NodeService nodeService;
	protected QName className;
	private boolean actOnWorkingCopies = false;

	public void init()
	{
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, className, new JavaBehaviour(this, "getCopyCallback"));
	}
  
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
	{
        return this;
	}

        @Override
        public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
		{
			NodeRef sourceNode = copyDetails.getSourceNodeRef();
			NodeRef targetNode = copyDetails.getTargetNodeRef();
			boolean flag = !actOnWorkingCopies && nodeService.exists(sourceNode) && nodeService.hasAspect(sourceNode, ContentModel.ASPECT_WORKING_COPY)
				|| !actOnWorkingCopies && nodeService.exists(targetNode) && nodeService.hasAspect(targetNode, ContentModel.ASPECT_WORKING_COPY);
			qNameConverter = new WorkflowQNameConverter(namespaceService);
			if(ignoredFields!=null)
			{
				for(String field : ignoredFields)
				{
					QName property = qNameConverter.mapNameToQName(field);
					boolean isIgnoreWorkingCopyProperty = flag ? ignorePropertiesOnWorkingCopy.contains(field) : false;
					if(!isIgnoreWorkingCopyProperty)
					{
						if(properties.containsKey(property))
						{
							properties.remove(property);
						}
						else
						{
							if(copyDetails.getSourceNodeProperties().containsKey(property))
							{
								copyDetails.getSourceNodeProperties().remove(property);
							}
						}
					}
				}
			}
			
			Map<QName, Serializable> propertiesNotNull = new HashMap<QName, Serializable> ();
			for(Map.Entry<QName, Serializable> entry : properties.entrySet())
			{
				if(properties.get(entry.getKey())!=null || !"".equals(properties.get(entry.getKey())))
				{
					propertiesNotNull.put(entry.getKey(), properties.get(entry.getKey()));
				}
			}
			if(propertiesNotNull.size()>0)
			{
				properties.clear();
				properties.putAll(propertiesNotNull);
			}
			return properties;
		}

        @Override
        public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(QName classQName, CopyDetails copyDetails, CopyAssociationDetails assocCopyDetails) {
            if (ignoredAssociations != null) {
                for (String assocField : ignoredAssociations) {
                    QName fullFieldName = qNameConverter.mapNameToQName(assocField);
                    if (assocCopyDetails.getAssocRef().getTypeQName().equals(fullFieldName)) {
                        return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(AssocCopySourceAction.IGNORE, AssocCopyTargetAction.USE_COPIED_TARGET);
                    }
                }
            }

            return super.getAssociationCopyAction(classQName, copyDetails, assocCopyDetails);
        }

    public void setIgnoredFields(List<String> ignoredFields) {
        this.ignoredFields = ignoredFields;
    }

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setClassName(QName className) {
		this.className = className;
	}
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

    public List<String> getIgnoredAssociations() {
        return ignoredAssociations;
    }

    public void setIgnoredAssociations(List<String> ignoredAssociations) {
        this.ignoredAssociations = ignoredAssociations;
    }

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActOnWorkingCopies(boolean actOnWorkingCopies) {
		this.actOnWorkingCopies = actOnWorkingCopies;
	}

	public void setIgnorePropertiesOnWorkingCopy(List<String> ignorePropertiesOnWorkingCopy) {
		this.ignorePropertiesOnWorkingCopy = ignorePropertiesOnWorkingCopy;
	}
}
