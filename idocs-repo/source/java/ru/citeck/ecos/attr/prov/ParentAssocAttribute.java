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
package ru.citeck.ecos.attr.prov;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.attr.SingleAttributeProvider;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.node.NodeInfo;

public class ParentAssocAttribute extends SingleAttributeProvider<QName> {

    public ParentAssocAttribute() {
        super(AttributeModel.ATTR_PARENT_ASSOC);
    }

    @Override
    public QName getValue(NodeRef nodeRef) {
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
        return parentAssoc.getTypeQName();
    }

    @Override
    protected void setValue(NodeRef nodeRef, QName newParentAssoc) {
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
        nodeService.moveNode(nodeRef, parentAssoc.getParentRef(), newParentAssoc, parentAssoc.getQName());
    }

    @Override
    protected void setValue(NodeInfo nodeInfo, QName newParentAssoc) {
        nodeInfo.setParentAssoc(newParentAssoc);
    }

}
