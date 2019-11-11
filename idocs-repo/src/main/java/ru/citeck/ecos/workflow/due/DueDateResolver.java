package ru.citeck.ecos.workflow.due;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Date;

/**
 * @author Roman Makarskiy
 */
public interface DueDateResolver {

    Date getDueDate(NodeRef document, NodeRef assignee, Date defaultDueDate, String key);

    boolean conditionSatisfied(NodeRef document, NodeRef assignee, String key);

}
