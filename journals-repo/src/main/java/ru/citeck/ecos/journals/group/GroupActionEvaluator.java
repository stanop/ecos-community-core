package ru.citeck.ecos.journals.group;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public abstract class GroupActionEvaluator {

    protected GroupActionService groupActionService;
    protected NamespaceService namespaceService;
    protected WorkflowService workflowService;
    protected NodeService nodeService;
    protected PersonService personService;


    public void init() {
        groupActionService.register(this);
    }

    public abstract String getActionId();

    public abstract void invoke(NodeRef nodeRef, Map<String, String> params);

    public abstract Map<NodeRef, GroupActionStatus> invokeBatch(List<NodeRef> nodeRefs, Map<String, String> params);

    public abstract boolean isApplicable(NodeRef nodeRef, Map<String, String> params);

    public abstract String[] getMandatoryParams();

    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
