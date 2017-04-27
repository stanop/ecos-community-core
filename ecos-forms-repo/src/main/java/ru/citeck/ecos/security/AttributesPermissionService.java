package ru.citeck.ecos.security;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 */
public interface AttributesPermissionService {
    boolean isFieldVisible(QName fieldQName, NodeRef caseRef, String mode);

    boolean isFieldEditable(QName fieldQName, NodeRef caseRef, String mode);

    void reloadDefinition(NodeRef matrixDefinitionRef);

    void reloadAllDefinitions();
}
