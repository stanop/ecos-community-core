package ru.citeck.ecos.workflow.due;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.spring.registry.MappingRegistry;

import java.util.Date;

/**
 * Activiti bean service, which allow get due date for specified assignee.
 * Due date receipt based on resolvers ({@link DueDateResolver}), implementation of which must be registered in
 * registry ("activiti.due-date-resolvers.mappingRegistry").
 * <p>
 * The search for resolvers occurs by condition, the first one is returned. If no resolvers are found -
 * returns {@link DefaultDueDateResolver}
 *
 * @author Roman Makarskiy
 */
@Slf4j
@Component
public class ActivitiDueDateService {

    private static final DefaultDueDateResolver defaultResolver = new DefaultDueDateResolver();

    @Autowired
    @Qualifier("activiti.due-date-resolvers.mappingRegistry")
    private MappingRegistry<String, DueDateResolver> filterRegistry;

    public Date get(Object document, Date defaultDueDate, String key, Object... assignees) {
        NodeRef documentRef = resolveRef(document);
        NodeRef assigneeRef = getFirstNonNullAssignee(assignees);


        return filterRegistry.getMapping().values()
                .stream()
                .filter(dueDateResolver -> dueDateResolver.conditionSatisfied(documentRef, assigneeRef, key))
                .findFirst()
                .orElse(defaultResolver)
                .getDueDate(documentRef, assigneeRef, defaultDueDate, key);
    }

    private NodeRef resolveRef(Object ref) {
        if (ref == null) {
            return null;
        }

        if (ref instanceof NodeRef) {
            return (NodeRef) ref;
        }

        if (ref instanceof ScriptNode) {
            return ((ScriptNode) ref).getNodeRef();
        }

        if (ref instanceof String) {
            String strRef = (String) ref;
            if (NodeRef.isNodeRef(strRef)) {
                return new NodeRef(strRef);
            }
        }

        log.warn("Unsupported conversion to NodeRef. Value: {}", ref);
        return null;
    }

    private NodeRef getFirstNonNullAssignee(Object[] assignees) {
        if (assignees == null || assignees.length == 0) {
            return null;
        }

        for (Object assignee : assignees) {
            NodeRef resolved = resolveRef(assignee);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    private static class DefaultDueDateResolver implements DueDateResolver {
        @Override
        public Date getDueDate(NodeRef document, NodeRef assignee, Date defaultDueDate, String key) {
            return defaultDueDate;
        }

        @Override
        public boolean conditionSatisfied(NodeRef document, NodeRef assignee, String key) {
            return true;
        }
    }

}
