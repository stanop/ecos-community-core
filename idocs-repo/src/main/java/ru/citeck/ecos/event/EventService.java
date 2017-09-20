package ru.citeck.ecos.event;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public interface EventService {

    /**
     * Fire events which have nodeRef as source and type eventType.
     * @param eventSourceRef reference to event source and context of events conditions
     * @param eventType event type
     */
    void fireEvent(NodeRef eventSourceRef, String eventType);

    /**
     * Fire events which have nodeRef as source and type eventType.
     * @param eventSourceRef reference to event source
     * @param conditionContextRef context of events conditions
     * @param eventType event type
     */
    void fireEvent(NodeRef eventSourceRef, NodeRef conditionContextRef, String eventType);

    /**
     * Fire specified event. Context of conditions - event source
     * @param eventRef reference to event
     */
    void fireConcreteEvent(NodeRef eventRef);

    /**
     * Fire specified event.
     * @param eventRef reference to event
     * @param conditionContextRef context of event conditions
     */
    void fireConcreteEvent(NodeRef eventRef, NodeRef conditionContextRef);

    /**
     * Check event conditions
     * @param eventRef reference to event and context of event conditions
     * @return true if all conditions return true or false otherwise
     */
    boolean checkConditions(NodeRef eventRef);

    /**
     * Check event conditions
     * @param eventRef reference to event
     * @param conditionContextRef context of event conditions
     * @return true if all conditions return true or false otherwise
     */
    boolean checkConditions(NodeRef eventRef, NodeRef conditionContextRef);

    /**
     * Get all events which have nodeRef as source and type eventType
     * @param nodeRef reference to event source
     * @param eventType event type
     * @return list of references to events
     */
    List<NodeRef> getEvents(NodeRef nodeRef, String eventType);
}
