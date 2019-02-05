package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface EcosFormioModel {

    String NAMESPACE = "http://www.citeck.ru/model/ecos/formio/1.0";

    QName TYPE_FORM = QName.createQName(NAMESPACE, "form");

    QName PROP_FORM_KEY = QName.createQName(NAMESPACE, "formKey");
    QName PROP_CUSTOM_MODULE = QName.createQName(NAMESPACE, "customModule");
}
