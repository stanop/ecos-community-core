package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class AlfrescoMissingQNamesModel {

    public static final String CONTENT_NAMESPACE = "http://www.alfresco.org/model/content/1.0";
    public static final String CM_PREFIX = "cm";

    // PROPS
    public static final QName PROP_MIDDLE_NAME = QName.createQName(CONTENT_NAMESPACE, "middleName");
}
