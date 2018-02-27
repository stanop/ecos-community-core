package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface EcosModel {

    // model
    String ECOS_MODEL_PREFIX = "ecos";

    // namespace
    String NAMESPACE = "http://www.citeck.ru/model/content/ecos/1.0";

    // types
    QName TYPE_DOCUMENT = QName.createQName(NAMESPACE, "document");

    // aspects

    // properties
    QName PROP_OLD_PASS = QName.createQName(NAMESPACE, "oldPass");
    QName PROP_PASS = QName.createQName(NAMESPACE, "pass");
    QName PROP_PASS_VERIFY = QName.createQName(NAMESPACE, "passVerify");
    QName PROP_IS_PERSON_DISABLED = QName.createQName(NAMESPACE, "isPersonDisabled");

    // assocs

}
