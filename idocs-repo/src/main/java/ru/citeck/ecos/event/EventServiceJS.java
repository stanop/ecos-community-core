package ru.citeck.ecos.event;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class EventServiceJS extends AlfrescoScopableProcessorExtension {

    private EventService eventService;

    public void fireEvent(Object node, String eventType) {
        NodeRef nodeRef = JavaScriptImplUtils.getNodeRef(node);
        eventService.fireEvent(nodeRef, eventType);
    }

    public void fireEvent(Object node, Object conditionContext, String eventType) {
        NodeRef nodeRef = JavaScriptImplUtils.getNodeRef(node);
        NodeRef conditionContextRef = JavaScriptImplUtils.getNodeRef(conditionContext);
        eventService.fireEvent(nodeRef, conditionContextRef, eventType);
    }

    public void fireConcreteEvent(Object event) {
        NodeRef eventRef = JavaScriptImplUtils.getNodeRef(event);
        eventService.fireConcreteEvent(eventRef);
    }

    public void fireConcreteEvent(Object event, Object conditionContext) {
        NodeRef eventRef = JavaScriptImplUtils.getNodeRef(event);
        NodeRef conditionContextRef = JavaScriptImplUtils.getNodeRef(conditionContext);
        eventService.fireConcreteEvent(eventRef, conditionContextRef);
    }

    public boolean checkConditions(Object event) {
        NodeRef eventRef = JavaScriptImplUtils.getNodeRef(event);
        return eventService.checkConditions(eventRef);
    }

    public boolean checkConditions(Object event, Object conditionContext) {
        NodeRef eventRef = JavaScriptImplUtils.getNodeRef(event);
        NodeRef conditionContextRef = JavaScriptImplUtils.getNodeRef(conditionContext);
        return eventService.checkConditions(eventRef, conditionContextRef);
    }

    public ScriptNode[] getEvents(Object node, String eventType) {
        NodeRef nodeRef = JavaScriptImplUtils.getNodeRef(node);
        List<NodeRef> events = eventService.getEvents(nodeRef, eventType);
        return JavaScriptImplUtils.wrapNodes(events, this);
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
