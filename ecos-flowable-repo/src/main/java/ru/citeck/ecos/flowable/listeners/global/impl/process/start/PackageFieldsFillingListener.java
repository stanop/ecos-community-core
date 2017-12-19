package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import java.io.Serializable;
import java.util.Map;

/**
 * Package fields filling listener
 */
public class PackageFieldsFillingListener implements GlobalStartExecutionListener {

    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_PREFIX = "flowable$";

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Namespace prefix resolver
     */
    private NamespacePrefixResolver namespaceService;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        NodeRef packageRef = FlowableListenerUtils.getWorkflowPackage(delegateExecution);
        Map<QName, Serializable> props = nodeService.getProperties(packageRef);
        boolean propsChanged = false;
        if(!props.containsKey(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID)) {
            props.put(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessInstanceId());
            propsChanged = true;
        }
        if(!props.containsKey(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME)) {
            props.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessDefinitionId().split(":")[0]);
            propsChanged = true;
        }
        if(propsChanged) {
            nodeService.setProperties(packageRef, props);
        }
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set namespace prefix resolver
     * @param namespaceService Namespace prefix resolver
     */
    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
        this.namespaceService = namespaceService;
    }
}
