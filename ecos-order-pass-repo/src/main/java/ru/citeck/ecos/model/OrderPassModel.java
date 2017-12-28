package ru.citeck.ecos.model;


import org.alfresco.service.namespace.QName;

public interface OrderPassModel {
    String NAMESPACE = "http://www.citeck.ru/model/content/order-pass/1.0";
    QName ORDER_PASS_TYPE = QName.createQName(NAMESPACE, "orderPass");

    QName VISITOR_FULL_NAME_PROP = QName.createQName(NAMESPACE, "visitorFullName");
    QName VISITOR_ORGANIZATION_PROP = QName.createQName(NAMESPACE, "visitorOrganization");
    QName VISITING_DATE_PROP = QName.createQName(NAMESPACE, "visitingDate");
    QName CAR_BRAND_AND_NUMBER_PROP = QName.createQName(NAMESPACE, "carBrandAndNumber");
    QName VISITOR_DOCUMENT_PROP = QName.createQName(NAMESPACE, "visitorDocument");
    QName INITIATOR_DEPARTMENT_PROP = QName.createQName(NAMESPACE, "initiatorDepartment");

    QName INITIATOR_ASSOC = QName.createQName(NAMESPACE, "initiator");


}
