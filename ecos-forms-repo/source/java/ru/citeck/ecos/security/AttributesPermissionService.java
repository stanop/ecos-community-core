package ru.citeck.ecos.security;

import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.invariants.view.NodeView;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 */
public interface AttributesPermissionService {
    public NodeView processNodeView(NodeRef nodeRef, NodeView nodeView);
}
