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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ScriptPermissionService extends BaseScopableProcessorExtension {

    private ServiceRegistry services;

    public String setPermission(
            String nodeRefId,
            String permission,
            String authority,
            String allow) {
        NodeRef nodeRef = new NodeRef(nodeRefId);
        this.services.getPermissionService().setPermission(
                nodeRef,
                authority,
                permission,
                new Boolean(allow)
        );
        return "";
    }

    public String deletePermission(
            final String nodeRefId,
            final String permission,
            final String authority) {
        NodeRef nodeRef = new NodeRef(nodeRefId);
        this.services.getPermissionService().deletePermission(
                nodeRef,
                authority,
                permission
        );
        return "";
    }

    public String clearPermissions(
            final String nodeRefId,
            final String authority) {
        NodeRef nodeRef = new NodeRef(nodeRefId);
        this.services.getPermissionService().clearPermission(
                nodeRef,
                authority
        );
        return "";
    }

    //store:

    public String deletePermissions(
            final String storeProtocol,
            final String storeId) {
        StoreRef storeRef = new StoreRef(storeProtocol, storeId);
        this.services.getPermissionService().deletePermissions(
                storeRef
        );
        return "";
    }

    public String clearPermission(
            final String storeProtocol,
            final String storeId,
            final String authority) {
        StoreRef storeRef = new StoreRef(storeProtocol, storeId);
        this.services.getPermissionService().clearPermission(
                storeRef,
                authority
        );
        return "";
    }


    //Inherit:

    public String setInheritParentPermissions(
            final String nodeRefId,
            final String inheritParentPermissions) {
        NodeRef nodeRef = new NodeRef(nodeRefId);
        this.services.getPermissionService().setInheritParentPermissions(
                nodeRef,
                new Boolean(inheritParentPermissions)
        );
        return "";
    }

    public String getInheritParentPermissions(final String nodeRefId) {
        NodeRef nodeRef = new NodeRef(nodeRefId);
        Boolean result = this.services.getPermissionService().getInheritParentPermissions(nodeRef);
        return result.toString();
    }



    public ServiceRegistry getServices() {
        return services;
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
    }
}
