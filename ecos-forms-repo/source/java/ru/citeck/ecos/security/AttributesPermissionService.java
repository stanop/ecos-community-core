package ru.citeck.ecos.security;

import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.view.NodeView;

import java.util.List;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 */
public interface AttributesPermissionService {
    public boolean isFieldVisible(QName fieldQName);

    public boolean isFieldEditable(QName fieldQName);
}
