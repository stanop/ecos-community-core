package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface OrdersModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/orders/common/1.0";

    public static final QName TYPE_INTERNAL = QName.createQName(NAMESPACE, "internal");

    public static final QName PROP_LAST_CORRECT_OUTCOME = QName.createQName(NAMESPACE, "lastCorrectOutcome");

}
