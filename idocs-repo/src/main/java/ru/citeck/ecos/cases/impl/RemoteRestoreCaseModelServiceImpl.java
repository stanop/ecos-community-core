package ru.citeck.ecos.cases.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.cases.RemoteCaseModelService;
import ru.citeck.ecos.cases.RemoteRestoreCaseModelService;
import ru.citeck.ecos.dto.*;
import ru.citeck.ecos.model.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remote restore case model service
 */
public class RemoteRestoreCaseModelServiceImpl implements RemoteRestoreCaseModelService {

    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Remote case model service
     */
    private RemoteCaseModelService remoteCaseModelService;

    /**
     * Restore case models
     * @param documentRef Document reference
     */
    @Override
    public void restoreCaseModels(NodeRef documentRef) {
        if (!nodeService.exists(documentRef)) {
            return;
        }
        List<CaseModelDto> caseModels = remoteCaseModelService.getCaseModelsByNodeRef(documentRef, true);
        for (CaseModelDto caseModelDto : caseModels) {
            List<CaseModelDto> childCaseModels = loadChildCases(caseModelDto);
            caseModelDto.setChildCases(childCaseModels);
        }
        /** Restore data */
        for (CaseModelDto caseModelDto : caseModels) {
            restoreCaseModelNodeRef(caseModelDto, documentRef);
        }
        /** Remote useless data and set flags */
        nodeService.setProperty(documentRef, IdocsModel.PROP_DOCUMENT_CASE_COMPLETED, false);
        nodeService.setProperty(documentRef, IdocsModel.PROP_CASE_MODELS_SENT, false);
        remoteCaseModelService.deleteCaseModelsByDocumentId(documentRef.getId());
    }

    /**
     * Load child cases
     * @param parentCaseModel Parent case model
     * @return List of child cases
     */
    private List<CaseModelDto> loadChildCases(CaseModelDto parentCaseModel) {
        List<CaseModelDto> caseModels = remoteCaseModelService.getCaseModelsByNodeRef(new NodeRef(WORKSPACE_PREFIX + parentCaseModel.getNodeUUID()), true);
        for (CaseModelDto caseModelDto : caseModels) {
            List<CaseModelDto> childCaseModels = loadChildCases(caseModelDto);
            caseModelDto.setChildCases(childCaseModels);
        }
        return caseModels;
    }

    /**
     * Restore case model node reference
     * @param caseModelDto Case model data transfer object
     * @param parentNodeRef Parent node reference
     */
    private void restoreCaseModelNodeRef(CaseModelDto caseModelDto, NodeRef parentNodeRef) {
        /** Create properties map */
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_CREATED, caseModelDto.getCreated());
        properties.put(ContentModel.PROP_CREATOR, caseModelDto.getCreator());
        properties.put(ContentModel.PROP_MODIFIED, caseModelDto.getModified());
        properties.put(ContentModel.PROP_MODIFIER, caseModelDto.getModifier());
        properties.put(ContentModel.PROP_TITLE, caseModelDto.getTitle());
        properties.put(ContentModel.PROP_DESCRIPTION, caseModelDto.getDescription());
        /** Case model properties */
        properties.put(ActivityModel.PROP_PLANNED_START_DATE, caseModelDto.getPlannedStartDate());
        properties.put(ActivityModel.PROP_PLANNED_END_DATE, caseModelDto.getPlannedEndDate());
        properties.put(ActivityModel.PROP_ACTUAL_START_DATE, caseModelDto.getActualStartDate());
        properties.put(ActivityModel.PROP_ACTUAL_END_DATE, caseModelDto.getActualEndDate());
        properties.put(ActivityModel.PROP_EXPECTED_PERFORM_TIME, caseModelDto.getExpectedPerformTime());
        properties.put(ActivityModel.PROP_MANUAL_STARTED, caseModelDto.getManualStarted());
        properties.put(ActivityModel.PROP_MANUAL_STOPPED, caseModelDto.getManualStopped());
        properties.put(ActivityModel.PROP_INDEX, caseModelDto.getIndex());
        properties.put(ActivityModel.PROP_AUTO_EVENTS, caseModelDto.getAutoEvents());
        properties.put(ActivityModel.PROP_REPEATABLE, caseModelDto.getRepeatable());
        properties.put(ActivityModel.PROP_TYPE_VERSION, caseModelDto.getTypeVersion());
        /** Create node */
        QName caseType = getCaseModelType(caseModelDto);
        ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef,
                ActivityModel.ASSOC_ACTIVITIES, ActivityModel.ASSOC_ACTIVITIES, caseType, properties);
        NodeRef caseModelRef = childAssociationRef.getChildRef();
        fillAdditionalInfo(caseModelDto, caseModelRef);
        /** Restore child cases */
        for (CaseModelDto childCaseModel : caseModelDto.getChildCases()) {
            restoreCaseModelNodeRef(childCaseModel, caseModelRef);
        }
    }

    /**
     * Fill additional info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalInfo(CaseModelDto caseModelDto, NodeRef caseModelRef) {
        if (caseModelDto instanceof StageDto) {
            fillAdditionalStageInfo((StageDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof ExecutionScriptDto) {
            fillAdditionalExecutionScriptInfo((ExecutionScriptDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof FailDto) {
            fillAdditionalFailInfo((FailDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof MailDto) {
            fillAdditionalMailInfo((MailDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof SetProcessVariableDto) {
            fillAdditionalSetProcessVariableInfo((SetProcessVariableDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof SetPropertyValueDto) {
            fillAdditionalSetPropertyValueInfo((SetPropertyValueDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof StartWorkflowDto) {
            fillAdditionalStartWorkflowInfo((StartWorkflowDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof SetCaseStatusDto) {
            fillAdditionalSetCaseStatusInfo((SetCaseStatusDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof CaseTimerDto) {
            fillAdditionalCaseTimerInfo((CaseTimerDto) caseModelDto, caseModelRef);
        }
        if (caseModelDto instanceof CaseTaskDto) {
            fillAdditionalCaseTaskInfo((CaseTaskDto) caseModelDto, caseModelRef);
        }
    }

    /**
     * Fill additional stage info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalStageInfo(StageDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, StagesModel.PROP_DOCUMENT_STATUS, caseModelDto.getDocumentStatus());
    }

    /**
     * Fill additional execution script info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalExecutionScriptInfo(ExecutionScriptDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, ActionModel.ExecuteScript.PROP_SCRIPT, caseModelDto.getExecuteScript());
    }

    /**
     * Fill additional info fail info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalFailInfo(FailDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, ActionModel.Fail.PROP_MESSAGE, caseModelDto.getFailMessage());
    }

    /**
     * Fill additional info mail info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalMailInfo(MailDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, ActionModel.Mail.PROP_TO, caseModelDto.getMailTo());
        nodeService.setProperty(caseModelRef, ActionModel.Mail.PROP_TO_MANY, caseModelDto.getToMany());
        nodeService.setProperty(caseModelRef, ActionModel.Mail.PROP_SUBJECT, caseModelDto.getSubject());
        nodeService.setProperty(caseModelRef, ActionModel.Mail.PROP_FROM, caseModelDto.getFromUser());
        nodeService.setProperty(caseModelRef, ActionModel.Mail.PROP_TEXT, caseModelDto.getMailText());
        nodeService.setProperty(caseModelRef, ActionModel.Mail.PROP_HTML, caseModelDto.getMailHtml());
    }

    /**
     * Fill additional info set process variable info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalSetProcessVariableInfo(SetProcessVariableDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, ActionModel.SetProcessVariable.PROP_VARIABLE, caseModelDto.getProcessVariableValue());
        nodeService.setProperty(caseModelRef, ActionModel.SetProcessVariable.PROP_VALUE, caseModelDto.getProcessVariableValue());
    }

    /**
     * Fill additional info set property value info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalSetPropertyValueInfo(SetPropertyValueDto caseModelDto, NodeRef caseModelRef) {
        QName propertyName = caseModelDto.getPropertyFullName() != null ? QName.createQName(caseModelDto.getPropertyFullName()) : null;
        nodeService.setProperty(caseModelRef, ActionModel.SetPropertyValue.PROP_PROPERTY, propertyName);
        nodeService.setProperty(caseModelRef, ActionModel.SetPropertyValue.PROP_VALUE, caseModelDto.getPropertyValue());
    }

    /**
     * Fill additional info start workflow info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalStartWorkflowInfo(StartWorkflowDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, ActionModel.StartWorkflow.PROP_WORKFLOW_NAME, caseModelDto.getWorkflowName());
    }

    /**
     * Fill additional info set case status info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalSetCaseStatusInfo(SetCaseStatusDto caseModelDto, NodeRef caseModelRef) {
        if (caseModelDto.getCaseStatus() != null) {
            NodeRef statusNodeRef = new NodeRef(WORKSPACE_PREFIX + caseModelDto.getCaseStatus().getNodeUUID());
            if (nodeService.exists(statusNodeRef)) {
                nodeService.createAssociation(caseModelRef, statusNodeRef, ActionModel.SetCaseStatus.PROP_STATUS);
            }
        }
    }

    /**
     * Fill additional info case timer info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalCaseTimerInfo(CaseTimerDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, CaseTimerModel.PROP_EXPRESSION_TYPE, caseModelDto.getExpressionType());
        nodeService.setProperty(caseModelRef, CaseTimerModel.PROP_TIMER_EXPRESSION, caseModelDto.getTimerExpression());
        nodeService.setProperty(caseModelRef, CaseTimerModel.PROP_DATE_PRECISION, caseModelDto.getDatePrecision());
        nodeService.setProperty(caseModelRef, CaseTimerModel.PROP_COMPUTED_EXPRESSION, caseModelDto.getComputedExpression());
        nodeService.setProperty(caseModelRef, CaseTimerModel.PROP_REPEAT_COUNTER, caseModelDto.getRepeatCounter());
        nodeService.setProperty(caseModelRef, CaseTimerModel.PROP_OCCUR_DATE, caseModelDto.getOccurDate());
    }

    /**
     * Fill additional info case task info
     * @param caseModelDto Case model data transfer object
     * @param caseModelRef Case model node reference
     */
    private void fillAdditionalCaseTaskInfo(CaseTaskDto caseModelDto, NodeRef caseModelRef) {
        nodeService.setProperty(caseModelRef, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME, caseModelDto.getWorkflowDefinitionName());
        nodeService.setProperty(caseModelRef, ICaseTaskModel.PROP_WORKFLOW_INSTANCE_ID, caseModelDto.getWorkflowInstanceId());
        nodeService.setProperty(caseModelRef, ICaseTaskModel.PROP_DEADLINE, caseModelDto.getDueDate());
        nodeService.setProperty(caseModelRef, ICaseTaskModel.PROP_PRIORITY, caseModelDto.getPriority());

        /** BPM Package */
        if (caseModelDto.getBpmPackage() != null) {
            NodeRef packageNodeRef = new NodeRef(WORKSPACE_PREFIX + caseModelDto.getBpmPackage().getNodeUUID());
            if (nodeService.exists(packageNodeRef)) {
                nodeService.createAssociation(caseModelRef, packageNodeRef, ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE);
            }
        }
    }

    /**
     * Get case model type
     * @param caseModelDto Case model data transfer object
     * @return Type name
     */
    private QName getCaseModelType(CaseModelDto caseModelDto) {
        if (caseModelDto instanceof StageDto) {
            return StagesModel.TYPE_STAGE;
        }
        if (caseModelDto instanceof ExecutionScriptDto) {
            return ActionModel.ExecuteScript.TYPE;
        }
        if (caseModelDto instanceof FailDto) {
            return ActionModel.Fail.TYPE;
        }
        if (caseModelDto instanceof MailDto) {
            return ActionModel.Mail.TYPE;
        }
        if (caseModelDto instanceof SetProcessVariableDto) {
            return ActionModel.SetProcessVariable.TYPE;
        }
        if (caseModelDto instanceof SetPropertyValueDto) {
            return ActionModel.SetPropertyValue.TYPE;
        }
        if (caseModelDto instanceof StartWorkflowDto) {
            return ActionModel.StartWorkflow.TYPE;
        }
        if (caseModelDto instanceof SetCaseStatusDto) {
            return ActionModel.SetCaseStatus.TYPE;
        }
        if (caseModelDto instanceof CaseTimerDto) {
            return CaseTimerModel.TYPE_TIMER;
        }
        if (caseModelDto instanceof CaseTaskDto) {
            return QName.createQName(((CaseTaskDto) caseModelDto).getTaskTypeFullName());
        }
        return ActivityModel.TYPE_ACTIVITY;
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set remote case model service
     * @param remoteCaseModelService Remote case model service
     */
    public void setRemoteCaseModelService(RemoteCaseModelService remoteCaseModelService) {
        this.remoteCaseModelService = remoteCaseModelService;
    }
}
