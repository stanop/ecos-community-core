package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface EcosProcessModel {

    String NAMESPACE = "http://www.citeck.ru/model/icaseEproc/1.0";

    QName ASPECT_HAS_PROCESS_ID = QName.createQName(NAMESPACE, "hasProcessId");
    QName ASPECT_HAS_STATE_ID = QName.createQName(NAMESPACE, "hasStateId");
    QName ASPECT_HAS_ACTIVITY_REF = QName.createQName(NAMESPACE, "hasActivityRef");
    QName ASPECT_HAS_ADDITIONAL_EVENT_DATA_ITEMS = QName.createQName(NAMESPACE, "hasAdditionalEventDataItems");
    QName ASPECT_HAS_EVENT_REF = QName.createQName(NAMESPACE, "hasEventRef");

    QName PROP_PROCESS_ID = QName.createQName(NAMESPACE, "processId");
    QName PROP_STATE_ID = QName.createQName(NAMESPACE, "stateId");
    QName PROP_ACTIVITY_REF = QName.createQName(NAMESPACE, "activityRef");
    QName PROP_EVENT_REF = QName.createQName(NAMESPACE, "eventRef");

    QName ASSOC_ADDITIONAL_EVENT_DATA_ITEMS = QName.createQName(NAMESPACE, "additionalEventDataItems");


}
