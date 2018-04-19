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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyComponentImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class CopyDocumentIgnoreAspectBehaviour implements OnCopyCompletePolicy {

	private PolicyComponent policyComponent;

	private NodeService nodeService;

	private List<QName> ignoreAspects;

	public void init() {
        for (QName ignoreAspect : ignoreAspects) {
            policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, ignoreAspect, new JavaBehaviour(this, "onCopyComplete"));
        }
    }

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode,	Map<NodeRef, NodeRef> copyMap) {
		if (notWorkingCopy(sourceNodeRef, targetNodeRef)) {
            if (nodeService.exists(targetNodeRef) && nodeService.hasAspect(targetNodeRef, classRef)) {
                nodeService.removeAspect(targetNodeRef, classRef);
            }
		}
	}

    private boolean notWorkingCopy(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
        return !nodeService.hasAspect(sourceNodeRef, ContentModel.ASPECT_WORKING_COPY) &&
                !nodeService.hasAspect(targetNodeRef, ContentModel.ASPECT_WORKING_COPY);
    }

	public void setPolicyComponent(PolicyComponentImpl policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setIgnoreAspects(List<QName> ignoreAspects) {
		this.ignoreAspects = ignoreAspects;
	}

}
