package ru.citeck.ecos.behavior.activity;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.activity.CaseActivityPolicies;
import ru.citeck.ecos.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseTaskBehavior implements CaseActivityPolicies.OnCaseActivityStartedPolicy,
                                         CaseActivityPolicies.OnCaseActivityStoppedPolicy {

    private static final Log log = LogFactory.getLog(CaseTaskBehavior.class);

    private Map<String, Map<String, String>> workflowProperties;

    private CaseActivityService caseActivityService;
    private NamespaceService namespaceService;
    private WorkflowService workflowService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.OnCaseActivityStartedPolicy.QNAME,
                ICaseTaskModel.TYPE_TASK,
                new JavaBehaviour(this, "onCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.OnCaseActivityStoppedPolicy.QNAME,
                ICaseTaskModel.TYPE_TASK,
                new JavaBehaviour(this, "onCaseActivityStopped", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
    }

    @Override
    public void onCaseActivityStarted(NodeRef taskRef) {

        String workflowDefinitionName = (String) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME);

        Map<String, String> propertiesMapping = workflowProperties.get(workflowDefinitionName);
        if(propertiesMapping == null){
            throw new AlfrescoRuntimeException(String.format("CaseTaskBehavior don't know about workflow %s", workflowDefinitionName));
        }

        String workflowDescription = (String) nodeService.getProperty(taskRef, ContentModel.PROP_TITLE);
        Date workflowDueDate = (Date) nodeService.getProperty(taskRef, ActivityModel.PROP_PLANNED_END_DATE);
        Integer workflowPriority = (Integer) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_PRIORITY);

        NodeRef wfPackage = workflowService.createPackage(null);
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProps.put(WorkflowModel.PROP_DUE_DATE, workflowDueDate);
        workflowProps.put(WorkflowModel.PROP_PRIORITY, workflowPriority);
        NodeRef parent = caseActivityService.getDocument(taskRef);

        this.nodeService.addChild(wfPackage, parent, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName((String) this.nodeService.getProperty(parent, ContentModel.PROP_NAME))));

        for(Map.Entry<String, String> entry : propertiesMapping.entrySet()) {
            QName key = QName.createQName(entry.getKey(), namespaceService);
            QName value = QName.createQName(entry.getValue(), namespaceService);

            Serializable property = nodeService.getProperty(taskRef, key);

            if(property != null) {
                workflowProps.put(value, property);
                continue;
            }

            ArrayList<NodeRef> assocs = getAssocs(taskRef, key);
            if(assocs.size() > 0) {
                workflowProps.put(value, assocs);
                continue;
            }

            //todo child assocs
        }

        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName(workflowDefinitionName);
        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), workflowProps);
        nodeService.setProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID, wfPath.getInstance().getId());
    }

    private ArrayList<NodeRef> getAssocs(NodeRef nodeRef, QName assocType) {
        ArrayList<NodeRef> result = new ArrayList<>();
        List<AssociationRef> assocsRefs = nodeService.getTargetAssocs(nodeRef, assocType);
        for(AssociationRef assocRef : assocsRefs) {
            NodeRef targetRef = assocRef.getTargetRef();
            QName targetType = nodeService.getType(targetRef);

            if(targetType.equals(ICaseRoleModel.TYPE_ROLE)) {
                result.addAll(getAssocs(targetRef, ICaseRoleModel.ASSOC_ASSIGNEES));
            } else {
                result.add(targetRef);
            }
        }
        return result;
    }

    @Override
    public void onCaseActivityStopped(NodeRef taskRef) {
        String workflowId = (String) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID);
        WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowId);

        if(workflowInstance != null && workflowInstance.isActive()) {
            workflowService.cancelWorkflow(workflowId);
        }
    }

    public void setWorkflowProperties(Map<String, Map<String, String>> workflowProperties) {
        this.workflowProperties = workflowProperties;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }
}
