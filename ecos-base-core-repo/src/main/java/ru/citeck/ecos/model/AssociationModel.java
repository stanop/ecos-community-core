package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface AssociationModel {

    String NAMESPACE = "http://www.citeck.ru/model/content/associations/1.0";

    QName ASSOC_ASSOCIATED_WITH = QName.createQName(NAMESPACE, "associatedWith");

}