package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class EcosTypeModel {

    // namespace
    public static final String NAMESPACE = "http://www.citeck.ru/model/ecos/type/1.0";

    // aspects
    public static final QName ASPECT_HAS_TYPE = QName.createQName(NAMESPACE, "hasType");

    // properties
    public static final QName PROP_TYPE = QName.createQName(NAMESPACE, "type");
}
