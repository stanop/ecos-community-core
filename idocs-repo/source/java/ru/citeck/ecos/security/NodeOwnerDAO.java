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
package ru.citeck.ecos.security;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import ru.citeck.ecos.model.GrantModel;

/**
 * Return node owner. If node has aspect grant:confiscated the real node owner is
 * stored in the property grant:owner.
 *
 * @author Alexander Nemerov
 */

public class NodeOwnerDAO extends BaseScopableProcessorExtension {

	private NodeService nodeService;
	private OwnableService ownableService;

	public String getOwner(NodeRef nodeRef) {
		if(nodeService.hasAspect(nodeRef, GrantModel.ASPECT_CONFISCATED)) {
			return (String) nodeService.getProperty(nodeRef, GrantModel.PROP_OWNER);
		} else {
			return ownableService.getOwner(nodeRef);
		}
	}
    public String getOwner(String nodeReference) {
        NodeRef nodeRef =new NodeRef(nodeReference);
        return getOwner(nodeRef);
    }
    public void setOwner(String nodeReference, String userName){
        NodeRef nodeRef =new NodeRef(nodeReference);
        setOwner(nodeRef,userName);
    }
    public void setOwner(NodeRef nodeRef, String userName){
        if(nodeService.hasAspect(nodeRef, GrantModel.ASPECT_CONFISCATED)) {
           nodeService.setProperty(nodeRef, GrantModel.PROP_OWNER, userName);
        } else {
            ownableService.setOwner(nodeRef, userName);
        }
    }

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

}
