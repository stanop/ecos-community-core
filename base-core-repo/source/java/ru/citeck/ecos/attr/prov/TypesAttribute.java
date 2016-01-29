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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.attr.SingleAttributeProvider;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.utils.DictionaryUtils;

public class TypesAttribute extends SingleAttributeProvider<List<?>> {

    public TypesAttribute() {
        super(AttributeModel.ATTR_TYPES);
    }

    @Override
    public List<QName> getValue(NodeRef nodeRef) {
        return DictionaryUtils.getAllNodeTypeNames(nodeRef, nodeService, dictionaryService);
    }

    @Override
    protected void setValue(NodeRef nodeRef, List<?> newTypes) {
        if(newTypes == null) return;
        QName oldType = nodeService.getType(nodeRef);
        QName newType = convertToQName(newTypes.get(0));
        if(oldType.equals(newType)) return;
        if(!dictionaryService.isSubClass(newType, oldType)) {
            throw new IllegalArgumentException("Can not change type from " + oldType + " to " + newType + ". Only type specializing is allowed");
        }
        nodeService.setType(nodeRef, newType);
    }

    @Override
    protected void setValue(NodeInfo nodeInfo, List<?> newTypes) {
        if(newTypes == null) return;
        nodeInfo.setType(convertToQName(newTypes.get(0)));
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        return QName.class;
    }

}
