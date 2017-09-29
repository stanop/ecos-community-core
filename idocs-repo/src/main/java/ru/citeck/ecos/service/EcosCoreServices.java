package ru.citeck.ecos.service;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.cmmn.service.CaseTemplateRegistry;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.icase.CaseElementService;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.icase.timer.CaseTimerService;

import static ru.citeck.ecos.service.CiteckServices.CITECK_NAMESPACE;

/**
 * @author Pavel Simonov
 */
public final class EcosCoreServices {

    public static final QName EVENT_SERVICE = QName.createQName(CITECK_NAMESPACE, "EcoS.EventService");
    public static final QName CASE_TIMER_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseTimerService");
    public static final QName CASE_TEMPLATE_REGISTRY = QName.createQName(CITECK_NAMESPACE, "caseTemplateRegistry");
    public static final QName CASE_ELEMENT_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseElementService");
    public static final QName CASE_STATUS_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseStatusService");
    public static final QName CASE_ACTIVITY_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseActivityService");

    public static EventService getEventService(ServiceRegistry services) {
        return (EventService) services.getService(EVENT_SERVICE);
    }

    public static CaseTimerService getCaseTimerService(ServiceRegistry services) {
        return (CaseTimerService) services.getService(CASE_TIMER_SERVICE);
    }

    public static CaseTemplateRegistry getCaseTemplateRegistry(ServiceRegistry services) {
        return (CaseTemplateRegistry) services.getService(CASE_TEMPLATE_REGISTRY);
    }

    public static CaseElementService getCaseElementService(ServiceRegistry services) {
        return (CaseElementService) services.getService(CASE_ELEMENT_SERVICE);
    }

    public static CaseStatusService getCaseStatusService(ServiceRegistry services) {
        return (CaseStatusService) services.getService(CASE_STATUS_SERVICE);
    }

    public static CaseActivityService getCaseActivityService(ServiceRegistry services) {
        return (CaseActivityService) services.getService(CASE_ACTIVITY_SERVICE);
    }
}
