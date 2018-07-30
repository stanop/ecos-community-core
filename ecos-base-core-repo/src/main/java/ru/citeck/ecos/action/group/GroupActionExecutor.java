package ru.citeck.ecos.action.group;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public abstract class GroupActionExecutor {

    protected GroupActionService groupActionService;
    protected NamespaceService namespaceService;
    protected WorkflowService workflowService;
    protected PersonService personService;
    protected NodeService nodeService;

    @PostConstruct
    public void init() {
        groupActionService.register(this);
    }

    public abstract String getActionId();

    public abstract void invoke(NodeRef nodeRef, Map<String, String> params);

    public abstract Map<NodeRef, GroupActionResult> invokeBatch(List<NodeRef> nodeRefs, Map<String, String> params);

    public abstract boolean isApplicable(NodeRef nodeRef, Map<String, String> params);

    public abstract String[] getMandatoryParams();

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.personService = serviceRegistry.getPersonService();
        this.nodeService = serviceRegistry.getNodeService();
    }
}
