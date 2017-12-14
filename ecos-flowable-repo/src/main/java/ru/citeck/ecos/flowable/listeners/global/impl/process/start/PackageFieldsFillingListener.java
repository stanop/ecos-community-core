package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

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
    private static final String PACKAGE_PREFIX = "bpm";
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
        QName workflowInstanceIdProp = QName.createQName(PACKAGE_PREFIX, "workflowInstanceId", namespaceService);
        if(!props.containsKey(workflowInstanceIdProp)) {
            props.put(workflowInstanceIdProp, FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessInstanceId());
            propsChanged = true;
        }
        QName workflowDefinitionNameProp = QName.createQName(PACKAGE_PREFIX, "workflowDefinitionName", namespaceService);
        if(!props.containsKey(workflowDefinitionNameProp)) {
            props.put(workflowDefinitionNameProp, FLOWABLE_ENGINE_PREFIX + delegateExecution.getProcessDefinitionId().split(":")[0]);
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
