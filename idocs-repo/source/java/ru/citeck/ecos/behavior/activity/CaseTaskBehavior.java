package ru.citeck.ecos.behavior.activity;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
import ru.citeck.ecos.action.ActionConstants;
import ru.citeck.ecos.behavior.ChainingJavaBehaviour;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseTaskBehavior implements CaseActivityPolicies.BeforeCaseActivityStartedPolicy,
                                         CaseActivityPolicies.OnCaseActivityResetPolicy {

    private static final Log log = LogFactory.getLog(CaseTaskBehavior.class);

    private final ValueConverter valueConverter = new ValueConverter();

    private Map<String, Map<String, String>> attributesMappingByWorkflow;
    private Map<String, List<String>> workflowTransmittedVariables;

    private CaseActivityService caseActivityService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private WorkflowService workflowService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.BeforeCaseActivityStartedPolicy.QNAME,
                ICaseTaskModel.TYPE_TASK,
                new ChainingJavaBehaviour(this, "beforeCaseActivityStarted", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
        this.policyComponent.bindClassBehaviour(
                CaseActivityPolicies.OnCaseActivityResetPolicy.QNAME,
                ICaseTaskModel.TYPE_TASK,
                new ChainingJavaBehaviour(this, "onCaseActivityReset", Behaviour.NotificationFrequency.EVERY_EVENT)
        );

        if (attributesMappingByWorkflow == null) {
            attributesMappingByWorkflow = new HashMap<>();
        }
        if (workflowTransmittedVariables == null) {
            workflowTransmittedVariables = new HashMap<>();
        }
    }

    @Override
    public void beforeCaseActivityStarted(NodeRef taskRef) {

        String workflowDefinitionName = (String) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME);

        if (!attributesMappingByWorkflow.containsKey(workflowDefinitionName)) {
            throw new AlfrescoRuntimeException(String.format("Task start failed. Attributes mapping is " +
                                                             "not registered for workflow %s.", workflowDefinitionName));
        }

        Map<QName, Serializable> workflowProperties = getWorkflowProperties(taskRef, workflowDefinitionName);

        NodeRef wfPackage = workflowService.createPackage(null);
        createOrReplaceAssociation(taskRef, wfPackage, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);
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

        setWorkflowPropertiesFromITask(workflowProperties, taskRef);

        Map<String, String> attributesMapping = attributesMappingByWorkflow.get(workflowDefinitionName);

        for (Map.Entry<String, String> entry : attributesMapping.entrySet()) {
            QName key = QName.createQName(entry.getKey(), namespaceService);
            QName value = QName.createQName(entry.getValue(), namespaceService);
            workflowProperties.put(value, getAttribute(taskRef, key, value));
        }

        List<String> transmittedParameters = workflowTransmittedVariables.get(workflowDefinitionName);
        if (transmittedParameters != null && transmittedParameters.size() > 0) {
            Map<String, Object> variables = AlfrescoTransactionSupport.getResource(ActionConstants.ACTION_CONDITION_VARIABLES);
            if (variables != null) {
                Object processVariablesObj = variables.get(ActionConstants.PROCESS_VARIABLES);
                if (processVariablesObj != null && processVariablesObj instanceof Map) {
                    Map<String, Serializable> processVariables = (Map) processVariablesObj;
                    for (String parameter : transmittedParameters) {
                        Serializable value = valueConverter.convertValueForRepo(processVariables.get(parameter));
                        if (value != null) {
                            workflowProperties.put(QName.createQName(parameter), value);
                        }
                    }
                }
            }
        }

        return workflowProperties;
    }

    private void setWorkflowPropertiesFromITask(Map<QName, Serializable> workflowProperties, NodeRef taskRef) {
        // get task properties
        String workflowDescription = (String) nodeService.getProperty(taskRef, ContentModel.PROP_TITLE);
        Date workflowDueDate = getWorkflowDueDate(taskRef);
        Integer workflowPriority = (Integer) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_PRIORITY);

        // set properties from task
        workflowProperties.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProperties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, workflowDueDate);
        workflowProperties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, workflowPriority);
    }

    private Date getWorkflowDueDate(NodeRef taskRef) {

        Date workflowDueDate = (Date) nodeService.getProperty(taskRef, ActivityModel.PROP_PLANNED_END_DATE);

        if (workflowDueDate == null && nodeService.hasAspect(taskRef, ActivityModel.ASPECT_SET_PLANNED_END_DATE)) {
            Date startDate = (Date) nodeService.getProperty(taskRef, ActivityModel.PROP_ACTUAL_START_DATE);
            if (startDate != null) {
                Integer numberDaysToDueDate = (Integer) nodeService.getProperty(taskRef, ActivityModel.PROP_DAYS_NUMBER_TO_PLANNED_END_DATE);
                if (numberDaysToDueDate != null && numberDaysToDueDate > 0) {
                    workflowDueDate = addDays(startDate, numberDaysToDueDate);
                    // set planned end date for icaseTask:task
                    nodeService.setProperty(taskRef, ActivityModel.PROP_PLANNED_END_DATE, workflowDueDate);
                }
            }
        }
        return workflowDueDate;
    }

    private static Date addDays(Date baseDate, int daysToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        return calendar.getTime();
    }



    /**
     * @param taskRef reference to task node
     * @param source  task attribute QName to get value
     * @param target  target attribute QName which will be set from source
     * @return Serializable value of source attribute or null if value is not defined
     * @throws AlfrescoRuntimeException when source or target QName is not association or property
     */
    private Serializable getAttribute(NodeRef taskRef, QName source, QName target) {
        PropertyDefinition propertyDef = dictionaryService.getProperty(source);
        if (propertyDef != null) {
            return nodeService.getProperty(taskRef, source);
        }
        AssociationDefinition associationDef = dictionaryService.getAssociation(source);
        if (associationDef != null) {
            AssociationDefinition targetAssoc = dictionaryService.getAssociation(target);
            if (targetAssoc == null) {
                throw new AlfrescoRuntimeException(
                        "Error occurred during workflow attribute getting. Make sure that QName \"" + target + "\" exists.");
            }
            ArrayList<NodeRef> assocs = getAssociations(taskRef, source);
            return targetAssoc.isTargetMany() ? assocs : (assocs.size() > 0 ? assocs.get(0) : null);
        }
        throw new AlfrescoRuntimeException(source + " is not a property or association (child associations is not allowed)");
    }

    private ArrayList<NodeRef> getAssociations(NodeRef nodeRef, QName assocType) {
        ArrayList<NodeRef> result = new ArrayList<>();
        List<AssociationRef> assocsRefs = nodeService.getTargetAssocs(nodeRef, assocType);
        for (AssociationRef assocRef : assocsRefs) {
            NodeRef targetRef = assocRef.getTargetRef();
            QName targetType = nodeService.getType(targetRef);

            if (targetType.equals(ICaseRoleModel.TYPE_ROLE)) {
                result.addAll(getAssociations(targetRef, ICaseRoleModel.ASSOC_ASSIGNEES));
            } else {
                result.add(targetRef);
            }
        }
        return result;
    }

    @Override
    public void onCaseActivityReset(NodeRef taskRef) {
        String workflowInstanceId = (String) nodeService.getProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID);

        if (workflowInstanceId != null && workflowService.getWorkflowById(workflowInstanceId).isActive()) {
            workflowService.cancelWorkflow(workflowInstanceId);
        }

        nodeService.setProperty(taskRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID, null);
        NodeRef wfPackage = RepoUtils.getFirstTargetAssoc(taskRef, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE, nodeService);
        if (wfPackage != null) {
            nodeService.removeAssociation(taskRef, wfPackage, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);
        }
    }

    private void createOrReplaceAssociation(NodeRef source, NodeRef target, QName assocType) {
        List<AssociationRef> assocs = nodeService.getTargetAssocs(source, assocType);
        if (assocs != null) {
            for (AssociationRef assoc : assocs) {
                nodeService.removeAssociation(source, assoc.getTargetRef(), assocType);
            }
        }
        nodeService.createAssociation(source, target, assocType);
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

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param workflowTransmittedVariables variables which would be transmitted from completed task to next
     */
    public void setWorkflowTransmittedVariables(Map<String, List<String>> workflowTransmittedVariables) {
        this.workflowTransmittedVariables = workflowTransmittedVariables;
    }

    /**
     * @param workflowTransmittedVariables variables which would be transmitted from completed task to next
     */
    public void registerWorkflowTransmittedVariables(Map<String, List<String>> workflowTransmittedVariables) {
        this.workflowTransmittedVariables.putAll(workflowTransmittedVariables);
    }
}
