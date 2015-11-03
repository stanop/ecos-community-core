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
package ru.citeck.ecos.behavior.common;

import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.List;

/**
 *
 */
public class PropResolver {

    private static NodeService nodeService;

    public static enum UniqueType {
        INCREMENT_POSTFIX,
        FILENAME_INCREMENT_POSTFIX
    }


    public static void setUniqueProperty(NodeRef nodeRef, QName propName, String propValue, UniqueType uniqueType) {
        if (UniqueType.INCREMENT_POSTFIX.equals(uniqueType)) {
            setUniquePropertyIncrementPostfixImpl(nodeRef, propName, propValue);
        } else if (UniqueType.FILENAME_INCREMENT_POSTFIX.equals(uniqueType)) {
            setUniqueFilenameIncrementPostfixImpl(nodeRef, propName, propValue);
        }
    }

    private static void setUniquePropertyIncrementPostfixImpl(NodeRef nodeRef, QName propName, String propValue) {
        if (null == propValue) {
            return;
        }
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        String resultValue = new String(propValue);
        for (int i = 1;; i++) {
            if (isPropValueExist(nodeRef, parent, propName, resultValue)) {
                resultValue = String.format("%s-%d", propValue, i);
            } else {
                nodeService.setProperty(nodeRef, propName, resultValue);
                return;
            }
        }
    }

    private static void setUniqueFilenameIncrementPostfixImpl(NodeRef nodeRef, QName propName, String fileName) {
        if (null == fileName) {
            return;
        }
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        int dotIndex = fileName.lastIndexOf(".");
        String name = (dotIndex > -1)? fileName.substring(0, dotIndex) : fileName;
        String ex = (dotIndex > -1)? fileName.substring(dotIndex, fileName.length()) : "";
        String resultValue = new String(fileName);
        for (int i = 1;; i++) {
            if (isPropValueExist(nodeRef, parent, propName, resultValue)) {
                resultValue = String.format("%s-%d%s", name, i, ex);
            } else {
                nodeService.setProperty(nodeRef, propName, resultValue);
                return;
            }
        }
    }

    private static boolean isPropValueExist(NodeRef target, NodeRef parent, QName propName, String propValue) {
        if (null == propValue) {
            return false;
        }
        List<ChildAssociationRef> childs = nodeService.getChildAssocs(parent);
        for(ChildAssociationRef child : childs) {
            if (target.equals(child.getChildRef())) {
                continue;
            }
            String value = (String) nodeService.getProperty(child.getChildRef(), propName);
            if (propValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void setNodeService(NodeService nodeService) {
        PropResolver.nodeService = nodeService;
    }

}
