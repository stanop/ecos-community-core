package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface ConfigModel {
    String NAMESPACE = "http://www.citeck.ru/model/config/1.0";

    QName TYPE_ECOS_CONFIG = QName.createQName(NAMESPACE, "ecosConfig");

    QName PROP_KEY = QName.createQName(NAMESPACE, "key");
    QName PROP_VALUE = QName.createQName(NAMESPACE, "value");
}
