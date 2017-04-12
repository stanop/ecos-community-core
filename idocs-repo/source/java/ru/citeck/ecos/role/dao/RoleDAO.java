package ru.citeck.ecos.role.dao;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.Set;

/**
 * @author Pavel Simonov
 */
public interface RoleDAO {

    Set<NodeRef> getAssignees(NodeRef caseRef, NodeRef roleRef);

    QName getRoleType();

}
