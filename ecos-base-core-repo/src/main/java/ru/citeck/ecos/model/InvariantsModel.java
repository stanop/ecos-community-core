package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface InvariantsModel {

    String NAMESPACE = "http://www.citeck.ru/model/invariants/1.0";

    QName ASPECT_DRAFT = QName.createQName(NAMESPACE, "draftAspect");

    QName PROP_IS_DRAFT = QName.createQName(NAMESPACE, "isDraft");
    QName PROP_CAN_RETURN_TO_DRAFT = QName.createQName(NAMESPACE, "canReturnToDraft");

}
