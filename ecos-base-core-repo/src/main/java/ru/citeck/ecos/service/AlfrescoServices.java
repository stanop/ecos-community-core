package ru.citeck.ecos.service;

import org.alfresco.cmis.client.impl.AlfrescoUtils;
import org.alfresco.service.namespace.QName;

/**
 * @author Pavel Simonov
 */
public interface AlfrescoServices {
    String ALFRESCO_NAMESPACE = AlfrescoUtils.ALFRESCO_NAMESPACE;

    QName MESSAGE_SERVICE = QName.createQName(ALFRESCO_NAMESPACE, "messageService");
}
