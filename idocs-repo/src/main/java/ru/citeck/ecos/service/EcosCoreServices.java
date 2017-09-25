package ru.citeck.ecos.service;

import org.alfresco.service.namespace.QName;

import static ru.citeck.ecos.service.CiteckServices.CITECK_NAMESPACE;

/**
 * @author Pavel Simonov
 */
public interface EcosCoreServices {

    QName EVENT_SERVICE = QName.createQName(CITECK_NAMESPACE, "EcoS.EventService");
    QName CASE_TIMER_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseTimerService");
    QName CASE_TEMPLATE_REGISTRY = QName.createQName(CITECK_NAMESPACE, "caseTemplateRegistry");

}
