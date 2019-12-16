package ru.citeck.ecos.document.sum;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.math.BigDecimal;

/**
 * Implements to define a sum resolver with custom logic.
 *
 * @author Roman Makarskiy
 */
public interface DocSumResolver {

    BigDecimal resolve(NodeRef document);

    QName getDocType();
}
