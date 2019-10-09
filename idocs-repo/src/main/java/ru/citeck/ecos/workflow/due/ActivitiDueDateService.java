package ru.citeck.ecos.workflow.due;

import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
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
@Component
public class ActivitiDueDateService {

    private static final DefaultDueDateResolver defaultResolver = new DefaultDueDateResolver();

    @Autowired
    @Qualifier("activiti.due-date-resolvers.mappingRegistry")
    private MappingRegistry<String, DueDateResolver> filterRegistry;

    public Date get(ActivitiScriptNode document, ActivitiScriptNode assignee, Date defaultDueDate, String key) {
        NodeRef documentRef = document != null ? document.getNodeRef() : null;
        NodeRef assigneeRef = assignee != null ? assignee.getNodeRef() : null;

        return filterRegistry.getMapping().values()
                .stream()
                .filter(dueDateResolver -> dueDateResolver.conditionSatisfied(documentRef, assigneeRef, key))
                .findFirst()
                .orElse(defaultResolver)
                .getDueDate(documentRef, assigneeRef, defaultDueDate, key);
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
