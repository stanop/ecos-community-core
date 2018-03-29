package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public final class EcosContentModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/ecos/content/1.0";
    public static final String PREFIX = "ecosCont";

    public static final QName PROP_DEPLOYED_CHECKSUM = QName.createQName(NAMESPACE, "deployedChecksum");

    public static final QName ASPECT_HAS_CONTENT_CHECKSUM = QName.createQName(NAMESPACE, "hasContentChecksum");
}
