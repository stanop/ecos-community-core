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
package ru.citeck.ecos.role;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.role.dao.RoleDAO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim Strizhov
 * @author Pavel Simonov
 */
public interface CaseRoleService {

    /**
     * Get all roles in specified case
     * @return list of roles
     */
    List<NodeRef> getRoles(NodeRef caseRef);

    /**
     * Get role by name in specified case
     * @return role or null if role not found
     */
    NodeRef getRole(NodeRef caseRef, String name);

    /**
     * Get role assignees by case reference and role name
     * @return role assignees
     * @throws IllegalArgumentException if role with specified name not found in case
     */
    Set<NodeRef> getAssignees(NodeRef caseRef, String roleName);

    /**
     * Get role assignees
     * @return role assignees
     */
    Set<NodeRef> getAssignees(NodeRef roleRef);

    /**
     * Search role by name and set assignees
     * @throws IllegalArgumentException if role with specified name not found in case
     */
    void setAssignees(NodeRef caseRef, String roleName, Collection<NodeRef> assignees);

    /**
     * Set role assignees
     */
    void setAssignees(NodeRef roleRef, Collection<NodeRef> assignees);

    /**
     * Remove all assignees from role
     */
    void removeAssignees(NodeRef roleRef);

    /**
     * Search role by name and remove all assignees
     * @throws IllegalArgumentException if role with specified name not found in case
     */
    void removeAssignees(NodeRef caseRef, String roleName);

    /**
     * Recalculate assignees for all dynamic roles in case
     */
    void updateRoles(NodeRef caseRef);

    /**
     * Recalculate assignees for role
     */
    void updateRole(NodeRef roleRef);

    /**
     * Search role by name and recalculate assignees
     * @throws IllegalArgumentException if role with specified name not found in case
     */
    void updateRole(NodeRef caseRef, String roleName);

    /**
     * Register role DAO
     */
    void register(RoleDAO roleDAO);
}
