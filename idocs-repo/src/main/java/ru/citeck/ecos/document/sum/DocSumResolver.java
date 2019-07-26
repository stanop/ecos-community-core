package ru.citeck.ecos.document.sum;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.math.BigDecimal;

/**
 * Extends this class to define a sum resolver with custom logic.<br>
 * Note: You must set a {@link #docType}.
 *
 * @author Roman Makarskiy
 */
public abstract class DocSumResolver {

    private QName docType;

    public abstract BigDecimal resolve(NodeRef document);

    public void setDocType(QName docType) {
        this.docType = docType;
    }

    public QName getDocType() {
        return this.docType;
    }
}
