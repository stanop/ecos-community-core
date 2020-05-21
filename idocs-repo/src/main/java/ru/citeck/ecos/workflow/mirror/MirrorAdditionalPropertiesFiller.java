package ru.citeck.ecos.workflow.mirror;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import ru.citeck.ecos.node.NodeInfo;

/**
 * <p>
 * This filler allow to set additional properties in mirror tasks from inherited modules.
 * To push additional properties implement interface and push data to {@code taskNodeInfo}.<br/>
 * For example:<br/>
 * <p>
 * {@code
 * taskNodeInfo.setProperty(WorkflowMirrorModel.PROP_ASSIGNEE, assigneeNodeRef);
 * }
 *
 * @author Roman Makarskiy
 */
public interface MirrorAdditionalPropertiesFiller {

    /**
     * @param task         current task
     * @param document     current document, may be null
     * @param taskNodeInfo NodeInfo of current task
     */
    void fill(WorkflowTask task, NodeRef document, NodeInfo taskNodeInfo);

}
