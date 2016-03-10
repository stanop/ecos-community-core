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
package ru.citeck.ecos.processor;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class SelectChildNodes extends AbstractDataBundleLine {

    private String parentNodeRef;

    private String childName;

    private NodeService nodeService;

    private Boolean onlyFirstNode;

    private String assocType;

    @Override
    public void init() {
        nodeService = serviceRegistry.getNodeService();
    }

    @Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = input.needModel();
        HashMap<String, Object> newModel = new HashMap<String, Object>();
        newModel.putAll(model);

        NodeRef parent = new NodeRef((String) evaluateExpression(parentNodeRef, model));
        newModel.put("parentNode", parent.toString());
        if (nodeService.exists(parent)) {
            NodeRef childNode = nodeService.getChildByName(parent, QName.createQName(assocType, serviceRegistry.getNamespaceService()), (String) evaluateExpression(childName, model));
            newModel.put("childNode", childNode != null ? childNode.toString() : null);
        }
        return helper.getDataBundle(helper.getContentReader(input), newModel);
    }

    private QNamePattern createQNamePattern(String property, Map<String, Object> model) {
        return property == null || property.isEmpty() ?
                RegexQNamePattern.MATCH_ALL :
                QName.createQName((String) evaluateExpression(property, model));
    }

    public void setParentNodeRef(String parentNodeRef) {
        this.parentNodeRef = parentNodeRef;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public void setOnlyFirstNode(Boolean onlyFirstNode) {
        this.onlyFirstNode = onlyFirstNode;
    }

    public void setAssocType(String assocType) {
        this.assocType = assocType;
    }
}
