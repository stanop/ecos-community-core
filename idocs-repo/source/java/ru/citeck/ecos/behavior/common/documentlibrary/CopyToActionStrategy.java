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
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;

/**
 *
 */
public abstract class CopyToActionStrategy {

    protected CopyService copyService;
    protected NodeService nodeService;

    public abstract boolean evaluate(NodeRef source, NodeRef destination);

    public abstract String copy(NodeRef source, NodeRef destination);


    public NodeRef copyAndRename(NodeRef source, NodeRef destination, boolean deepCopy) {
        ParameterCheck.mandatory("Source Node", destination);
        ParameterCheck.mandatory("Destination Node", destination);
        NodeRef target = copyService.copyAndRename(
                source,
                destination,
                ContentModel.ASSOC_CONTAINS,
                null,
                deepCopy
        );
        return target;
    }


    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public CopyService getCopyService() {
        return copyService;
    }

    public void setCopyService(CopyService copyService) {
        this.copyService = copyService;
    }
}
