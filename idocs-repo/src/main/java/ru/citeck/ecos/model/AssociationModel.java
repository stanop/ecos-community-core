package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface AssociationModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/content/associations/1.0";

    public static final QName ASSOC_PRIMARY = QName.createQName(NAMESPACE, "primary");

}