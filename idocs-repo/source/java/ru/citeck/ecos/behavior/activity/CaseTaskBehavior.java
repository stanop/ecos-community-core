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
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseTaskBehavior implements CaseActivityPolicies.OnCaseActivityStartedPolicy {

    private static final Log log = LogFactory.getLog(CaseTaskBehavior.class);

    private Map<String, Map<String, String>> attributesMappingByWorkflow;

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
    }

    @Override
    public void onCaseActivityStarted(NodeRef taskRef) {

        String workflowDefinitionName = (String) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME);

        if(!attributesMappingByWorkflow.containsKey(workflowDefinitionName)) {
            throw new AlfrescoRuntimeException(String.format("CaseTaskBehavior don't know about workflow %s", workflowDefinitionName));
        }

        Map<QName, Serializable> workflowProperties = getWorkflowProperties(taskRef, workflowDefinitionName);

        NodeRef wfPackage = workflowService.createPackage(null);
        workflowProperties.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

        NodeRef parent = caseActivityService.getDocument(taskRef);

        this.nodeService.addChild(wfPackage, parent, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName((String) this.nodeService.getProperty(parent, ContentModel.PROP_NAME))));

        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName(workflowDefinitionName);
        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), workflowProperties);
        nodeService.setProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID, wfPath.getInstance().getId());
    }

    private Map<QName, Serializable> getWorkflowProperties(NodeRef taskRef, String workflowDefinitionName) {

        Map<QName, Serializable> workflowProperties = new HashMap<QName, Serializable>();

        String workflowDescription = (String) nodeService.getProperty(taskRef, ContentModel.PROP_TITLE);
        Date workflowDueDate = (Date) nodeService.getProperty(taskRef, ActivityModel.PROP_PLANNED_END_DATE);
        Integer workflowPriority = (Integer) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_PRIORITY);

        workflowProperties.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProperties.put(WorkflowModel.PROP_DUE_DATE, workflowDueDate);
        workflowProperties.put(WorkflowModel.PROP_PRIORITY, workflowPriority);

        Map<String, String> attributesMapping = attributesMappingByWorkflow.get(workflowDefinitionName);

        for(Map.Entry<String, String> entry : attributesMapping.entrySet()) {
            QName key = QName.createQName(entry.getKey(), namespaceService);
            QName value = QName.createQName(entry.getValue(), namespaceService);

            Serializable property = nodeService.getProperty(taskRef, key);

            if(property != null) {
                workflowProperties.put(value, property);
                continue;
            }

            workflowProperties.put(value, getAssociations(taskRef, key));
        }

        return workflowProperties;
    }

    private ArrayList<NodeRef> getAssociations(NodeRef nodeRef, QName assocType) {
        ArrayList<NodeRef> result = new ArrayList<>();
        List<AssociationRef> assocsRefs = nodeService.getTargetAssocs(nodeRef, assocType);
        for(AssociationRef assocRef : assocsRefs) {
            NodeRef targetRef = assocRef.getTargetRef();
            QName targetType = nodeService.getType(targetRef);

            if(targetType.equals(ICaseRoleModel.TYPE_ROLE)) {
                result.addAll(getAssociations(targetRef, ICaseRoleModel.ASSOC_ASSIGNEES));
            } else {
                result.add(targetRef);
            }
        }
        return result;
    }

    public void registerAttributesMapping(Map<String, Map<String, String>> attributesMappingByWorkflow) {
        this.attributesMappingByWorkflow.putAll(attributesMappingByWorkflow);
    }

    public void setAttributesMappingByWorkflow(Map<String, Map<String, String>> attributesMappingByWorkflow) {
        this.attributesMappingByWorkflow = attributesMappingByWorkflow;
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
