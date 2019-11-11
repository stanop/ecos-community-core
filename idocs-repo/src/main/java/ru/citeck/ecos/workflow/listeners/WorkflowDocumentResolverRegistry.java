package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.VariableScope;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class WorkflowDocumentResolverRegistry {
    public final static String BEAN_NAME = "ecos.workflowDocumentResolverRegistry";

    private NodeService nodeService;
    private WorkflowService workflowService;

    private Map<String, IDocumentResolver> registry = new HashMap<>();
    private IDocumentResolver defaultResolver = new DefaultDocumentResolver();

    public IDocumentResolver getResolver(String wfName) {
        IDocumentResolver explicitlyRegistered = registry.get(wfName);
        if (explicitlyRegistered != null) {
            return explicitlyRegistered;
        }
        return defaultResolver;
    }

    public IDocumentResolver getResolver(DelegateExecution execution) {
        WorkflowDefinition workflowDefinition = ListenerUtils.tryGetWorkflowDefinition(execution, workflowService);
        String workflowDefinitionName = null;
        if (workflowDefinition != null) {
            workflowDefinitionName = workflowDefinition.getName();
        }
        return getResolver(workflowDefinitionName);
    }

    public void register(String wfName, IDocumentResolver resolver) {
        registry.put(wfName, resolver);
    }

    public class DefaultDocumentResolver implements IDocumentResolver {

        @Override
        public NodeRef getDocument(VariableScope execution) {
            return ListenerUtils.getDocument(execution, nodeService);
        }

        @Override
        public NodeRef getDocument(NodeRef wfPackage) {
            return ListenerUtils.getDocumentByPackage(wfPackage, nodeService);
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        workflowService = serviceRegistry.getWorkflowService();
    }
}
