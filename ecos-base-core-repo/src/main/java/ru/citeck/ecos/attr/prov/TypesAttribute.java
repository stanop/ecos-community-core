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
        QName newType = getLastType(newTypes);
        if (newType != null) {
            QName oldType = nodeService.getType(nodeRef);
            if (!oldType.equals(newType)) {
                if (!dictionaryService.isSubClass(newType, oldType)) {
                    throw new IllegalArgumentException("Can not change type from " + oldType + " to " +
                                                       newType + ". Only type specializing is allowed");
                }
                nodeService.setType(nodeRef, newType);
            }
        }
    }

    @Override
    protected void setValue(NodeInfo nodeInfo, List<?> newTypes) {
        QName type = getLastType(newTypes);
        if (type != null) {
            nodeInfo.setType(type);
        }
    }

    private QName getLastType(List<?> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        QName type = convertToQName(types.get(0));
        for (int i = 1; i < types.size(); i++) {
            QName otherType = convertToQName(types.get(i));
            if (!dictionaryService.isSubClass(type, otherType)) {
                type = otherType;
            }
        }
        return type;
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        return QName.class;
    }

}
