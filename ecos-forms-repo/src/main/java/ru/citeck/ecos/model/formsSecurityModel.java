package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * @author Roman Makarskiy
 */
public interface formsSecurityModel {

    String NAMESPACE = "http://www.citeck.ru/model/formsSecurity/1.0";

    QName ASPECT_INCLUDE_CHILD_SECURITY = QName.createQName(NAMESPACE, "includeChildSecurity");

}
