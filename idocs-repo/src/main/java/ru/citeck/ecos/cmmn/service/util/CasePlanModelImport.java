package ru.citeck.ecos.cmmn.service.util;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.condition.Condition;
import ru.citeck.ecos.cmmn.condition.ConditionProperty;
import ru.citeck.ecos.cmmn.condition.ConditionsList;
import ru.citeck.ecos.cmmn.model.*;
import ru.citeck.ecos.icase.CaseConstants;
import ru.citeck.ecos.icase.CaseElementService;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.model.*;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.ru)
 */
public class CasePlanModelImport {
    private static final Logger logger = Logger.getLogger(CasePlanModelImport.class);
    public static final QName ASSOC_ACTIVITIES = QName.createQName("http://www.citeck.ru/model/activity/1.0", "activities");
    public static final QName ASSOC_COMPLETENESS_LEVELS = QName.createQName("http://www.citeck.ru/model/case/requirement/1.0", "completenessLevels");

    private NodeService nodeService;
    private CaseStatusService caseStatusService;
    private CaseElementService caseElementService;

    private Map<String, NodeRef> rolesRef;
    private Map<String, NodeRef> planItemsMapping = new HashMap<>();
    private Map<String, NodeRef> completnessLevelRefs = new HashMap<>();
    private boolean isCompletnessLevelsExists = false;

    public CasePlanModelImport(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseStatusService = EcosCoreServices.getCaseStatusService(serviceRegistry);
        this.caseElementService = EcosCoreServices.getCaseElementService(serviceRegistry);
    }

    public void importCasePlan(NodeRef caseRef, Case caseItem, Map<String, NodeRef> rolesRef) {

        logger.info("Importing case plan... caseRef: " + caseRef);

        importCaseElementTypes(caseRef, caseItem);

        planItemsMapping.put(caseItem.getId(), caseRef);
        Stage casePlanModel = caseItem.getCasePlanModel();
        this.rolesRef = rolesRef;

        if (casePlanModel.getOtherAttributes().get(CMMNUtils.QNAME_COMPLETNESS_LEVELS) != null) {
            isCompletnessLevelsExists = true;
            String completnessLevelsString = casePlanModel.getOtherAttributes().get(CMMNUtils.QNAME_COMPLETNESS_LEVELS);
            String[] completnessLevelsArray = completnessLevelsString.split(",");
            for (String comletnessLevel : completnessLevelsArray) {
                NodeRef nodeRef = CMMNUtils.extractNodeRefFromCmmnId(comletnessLevel);
                completnessLevelRefs.put(comletnessLevel, nodeRef);
                if (!nodeService.exists(nodeRef)) {
                    logger.error("Completness level with nodeRef = " + nodeRef + " doesn't exists!");
                    isCompletnessLevelsExists = false;
                }
            }
            if (isCompletnessLevelsExists) {
                for (NodeRef nodeRef : completnessLevelRefs.values()) {
                    nodeService.createAssociation(caseRef, nodeRef, ASSOC_COMPLETENESS_LEVELS);
                }
            }
        }

        List<JAXBElement<? extends TPlanItemDefinition>> definitions = casePlanModel.getPlanItemDefinition();
        for (JAXBElement<? extends TPlanItemDefinition> jaxbElement : definitions) {
            if (CMMNUtils.isTask(jaxbElement) || CMMNUtils.isProcessTask(jaxbElement)) {
                importTask(caseRef, (TTask) jaxbElement.getValue(), ASSOC_ACTIVITIES);
            } else if (CMMNUtils.isStage(jaxbElement)) {
                importStage(caseRef, (Stage) jaxbElement.getValue(), ASSOC_ACTIVITIES);
            } else if (CMMNUtils.isTimer(jaxbElement)) {
                importTimer(caseRef, (TTimerEventListener) jaxbElement.getValue(), ASSOC_ACTIVITIES);
            }
        }
        importEvents(casePlanModel);
    }

    private void importCaseElementTypes(NodeRef caseRef, Case caseItem) {

        String elementsStr = caseItem.getOtherAttributes().get(CMMNUtils.QNAME_ELEMENT_TYPES);

        if (StringUtils.isNotBlank(elementsStr)) {

            String[] elements = elementsStr.split(",");

            for (String element : elements) {
                NodeRef elementRef = caseElementService.getConfig(element);
                if (elementRef != null) {
                    caseElementService.addElement(elementRef, caseRef, CaseConstants.ELEMENT_TYPES);
                }
            }
        }
    }

    private void importTimer(NodeRef parentStageRef, TTimerEventListener timer, QName assocName) {
        QName nodeType = QName.createQName(timer.getOtherAttributes().get(CMMNUtils.QNAME_NODE_TYPE));
        NodeRef timerRef = nodeService.createNode(parentStageRef, assocName, assocName, nodeType).getChildRef();
        importAttributes(timer, timerRef);
        planItemsMapping.put(timer.getId(), timerRef);
    }

    private void importStage(NodeRef parentStageRef, Stage stage, QName assocName) {
        logger.debug("Importing stage: " + stage.getId());
        NodeRef stageNodeRef = nodeService.createNode(parentStageRef, assocName, assocName, StagesModel.TYPE_STAGE).getChildRef();
        stage.getOtherAttributes().put(CMMNUtils.QNAME_NEW_ID, stageNodeRef.toString());
        importAttributes(stage, stageNodeRef);
        planItemsMapping.put(stage.getId(), stageNodeRef);
        if (!stage.getPlanItemDefinition().isEmpty()) {
            for (JAXBElement<? extends TPlanItemDefinition> jaxbElement : stage.getPlanItemDefinition()) {
                if (CMMNUtils.isTask(jaxbElement) || CMMNUtils.isProcessTask(jaxbElement)) {
                    importTask(stageNodeRef, (TTask) jaxbElement.getValue(), ActivityModel.ASSOC_ACTIVITIES);
                } else if (CMMNUtils.isStage(jaxbElement)) {
                    importStage(stageNodeRef, (Stage) jaxbElement.getValue(), ActivityModel.ASSOC_ACTIVITIES);
                } else if (CMMNUtils.isTimer(jaxbElement)) {
                    importTimer(stageNodeRef, (TTimerEventListener) jaxbElement.getValue(), ASSOC_ACTIVITIES);
                }
            }
        }
        importEvents(stage);
        addCompletnessLevels(stage, stageNodeRef);
    }

    private void importTask(NodeRef parentStageRef, TTask task, QName assocName) {
        logger.debug("Importing task: " + task.getId());
        QName nodeType = QName.createQName(task.getOtherAttributes().get(CMMNUtils.QNAME_NODE_TYPE));
        NodeRef taskNodeRef = nodeService.createNode(parentStageRef, assocName, assocName, nodeType).getChildRef();
        addRoles(task, taskNodeRef);
        importAttributes(task, taskNodeRef);
        planItemsMapping.put(task.getId(), taskNodeRef);
        addCompletnessLevels(task, taskNodeRef);
    }

    private void addRoles(TTask task, NodeRef taskRef) {
        for (Map.Entry<javax.xml.namespace.QName, QName> entry : CMMNUtils.ROLES_ASSOCS_MAPPING.entrySet()) {
            String value = task.getOtherAttributes().get(entry.getKey());
            if (value != null && !value.isEmpty()) {
                String[] rolesArray = value.split(",");
                for (String roleId : rolesArray) {
                    if (rolesRef.get(roleId) != null) {
                        nodeService.createAssociation(taskRef, rolesRef.get(roleId), entry.getValue());
                    }
                }
            }
        }
    }

    private void addCompletnessLevels(TPlanItemDefinition definition, NodeRef nodeRef) {
        if (isCompletnessLevelsExists) {
            if (definition.getOtherAttributes().get(CMMNUtils.QNAME_START_COMPLETNESS_LEVELS) != null) {
                String value = definition.getOtherAttributes().get(CMMNUtils.QNAME_START_COMPLETNESS_LEVELS);
                if (!value.isEmpty()) {
                    String[] values = value.split(",");
                    for (String str : values) {
                        if (completnessLevelRefs.get(str) != null) {
                            nodeService.createAssociation(nodeRef, completnessLevelRefs.get(str), StagesModel.ASSOC_START_COMPLETENESS_LEVELS_RESTRICTION);
                        }
                    }
                }
            }
            if (definition.getOtherAttributes().get(CMMNUtils.QNAME_STOP_COMPLETNESS_LEVELS) != null) {
                String value = definition.getOtherAttributes().get(CMMNUtils.QNAME_STOP_COMPLETNESS_LEVELS);
                if (!value.isEmpty()) {
                    String[] values = value.split(",");
                    for (String str : values) {
                        if (completnessLevelRefs.get(str) != null) {
                            nodeService.createAssociation(nodeRef, completnessLevelRefs.get(str), StagesModel.ASSOC_STOP_COMPLETENESS_LEVELS_RESTRICTION);
                        }
                    }
                }
            }
        }
    }

    private void importAttributes(TPlanItemDefinition definition, NodeRef nodeRef) {
        Map<javax.xml.namespace.QName, String> attributes = definition.getOtherAttributes();

        Map<QName, Serializable> properties = new HashMap<>();
        if (attributes.get(CMMNUtils.QNAME_TITLE) != null) {
            properties.put(ContentModel.PROP_TITLE, definition.getOtherAttributes().get(CMMNUtils.QNAME_TITLE));
        }
        String description = definition.getOtherAttributes().get(CMMNUtils.QNAME_DESCRIPTION);
        if (description != null) {
            properties.put(ContentModel.PROP_DESCRIPTION, description);
        }
        if (definition.getName() != null) {
            properties.put(ContentModel.PROP_NAME, definition.getName());
        }
        for (javax.xml.namespace.QName key : attributes.keySet()) {
            if (!key.getNamespaceURI().equals(CMMNUtils.NAMESPACE)) {
                String value = attributes.get(key);
                properties.put(CMMNUtils.convertFromXMLQName(key), value.trim().isEmpty() ? null : value);
            }
        }
        nodeService.setProperties(nodeRef, properties);

        for (Map.Entry<javax.xml.namespace.QName, QName> entry : CMMNUtils.STATUS_ASSOCS_MAPPING.entrySet()) {
            String status = attributes.get(entry.getKey());
            if (status != null) {
                NodeRef statusRef = caseStatusService.getStatusByName(status);
                if (statusRef != null) {
                    nodeService.createAssociation(nodeRef, statusRef, entry.getValue());
                } else {
                    logger.error("Status " + status + " not found in system. Please create it and import the template again");
                }
            }
        }
    }

    private void importEvents(Stage parentStage) {

        Map<String, Sentry> stageSentries = new HashMap<>();
        for (Sentry sentry : parentStage.getSentry()) {
            stageSentries.put(sentry.getId(), sentry);
        }

        for (TPlanItem planItem : parentStage.getPlanItem()) {

            TPlanItemDefinition planItemDefinition = (TPlanItemDefinition) planItem.getDefinitionRef();
            NodeRef activityRef = planItemsMapping.get(planItemDefinition.getId());

            logger.debug("Importing events for " + planItem.getId());
            for (Sentry sentry : getEntrySentries(planItem, stageSentries)) {
                importEvent(activityRef, sentry, true);
            }
            for (Sentry sentry : getExitSentries(planItem, stageSentries)) {
                importEvent(activityRef, sentry, false);
            }
        }
    }

    private void importEvent(NodeRef activityRef, Sentry sentry, boolean isEntryEvent) {

        List<JAXBElement<? extends TOnPart>> onParts = sentry.getOnPart();
        JAXBElement<? extends TOnPart> onPart = onParts.get(0);
        QName nodeType = QName.createQName(onPart.getValue().getOtherAttributes().get(CMMNUtils.QNAME_NODE_TYPE));
        String sourceId = onPart.getValue().getOtherAttributes().get(CMMNUtils.QNAME_SOURCE_ID);
        NodeRef sourceRef = planItemsMapping.get(sourceId);
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(EventModel.PROP_TYPE, sentry.getOtherAttributes().get(CMMNUtils.QNAME_ORIGINAL_EVENT));
        properties.put(ContentModel.PROP_TITLE, onPart.getValue().getOtherAttributes().get(CMMNUtils.QNAME_TITLE));

        QName assocType = getEventAssocType(activityRef, onPart.getValue(), isEntryEvent);
        NodeRef eventRef = nodeService.createNode(activityRef, assocType,
                assocType, nodeType, properties).getChildRef();
        importUserEventProperties(sentry, eventRef);
        nodeService.createAssociation(eventRef, sourceRef, EventModel.ASSOC_EVENT_SOURCE);
        processIfPart(sentry, eventRef);
        processAuthorizedRoles((TPlanItemOnPart) onPart.getValue(), eventRef);
    }

    private QName getEventAssocType(NodeRef activityRef, TOnPart onPart, boolean isEntryEvent) {
        QName assocType;
        if (isEntryEvent) {
            String isRestartEvent = onPart.getOtherAttributes().get(CMMNUtils.QNAME_IS_RESTART_EVENT);
            if (Boolean.TRUE.toString().equals(isRestartEvent)) {
                assocType = ICaseEventModel.ASSOC_ACTIVITY_RESTART_EVENTS;
            } else {
                assocType = ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS;
            }
        } else {
            QName activityType = nodeService.getType(activityRef);
            if (CaseTimerModel.TYPE_TIMER.equals(activityType)) {
                assocType = ICaseEventModel.ASSOC_ACTIVITY_RESET_EVENTS;
            } else {
                assocType = ICaseEventModel.ASSOC_ACTIVITY_END_EVENTS;
            }
        }
        return assocType;
    }

    private List<Sentry> getEntrySentries(TPlanItem planItem, Map<String, Sentry> stageSentries) {
        List<Sentry> result = new ArrayList<>();
        result.addAll(CMMNUtils.criterionToSentries(planItem.getEntryCriterion()));
        String sentriesStr = planItem.getOtherAttributes().get(CMMNUtils.QNAME_ENTRY_SENTRY);
        result.addAll(CMMNUtils.stringToElements(sentriesStr, stageSentries));
        return result;
    }

    private List<Sentry> getExitSentries(TPlanItem planItem, Map<String, Sentry> stageSentries) {
        List<Sentry> result = new ArrayList<>();
        result.addAll(CMMNUtils.criterionToSentries(planItem.getExitCriterion()));
        String sentriesStr = planItem.getOtherAttributes().get(CMMNUtils.QNAME_EXIT_SENTRY);
        result.addAll(CMMNUtils.stringToElements(sentriesStr, stageSentries));
        return result;
    }

    private void importUserEventProperties(Sentry sentry, NodeRef event) {
        for (Map.Entry<javax.xml.namespace.QName, QName> entry : CMMNUtils.EVENT_PROPS_MAPPING.entrySet()) {
            Serializable value = CMMNUtils.convertValueForRepo(entry.getValue(), sentry.getOtherAttributes().get(entry.getKey()));
            if (value != null) {
                nodeService.setProperty(event, entry.getValue(), value);
            }
        }
    }

    private void processIfPart(Sentry sentry, NodeRef eventRef) {
        if (sentry.getIfPart() != null) {
            TIfPart ifPart = sentry.getIfPart();
            TExpression expression = ifPart.getCondition();
            String content = (String) expression.getContent().get(0);
            content = content.replace("<!CDATA[", "").replace("]]>", "");
            try {
                for (Condition condition : importConditions(content).getConditions()) {
                    QName conditionTypeQName = CMMNUtils.convertFromXMLQName(condition.getType());
                    Map<QName, Serializable> conditionProperties = new HashMap<>();
                    for (ConditionProperty conditionProperty : condition.getProperties()) {
                        QName propertyType = CMMNUtils.convertFromXMLQName(conditionProperty.getType());
                        conditionProperties.put(propertyType, conditionProperty.getValue());
                    }
                    nodeService.createNode(eventRef, EventModel.ASSOC_CONDITIONS,
                            EventModel.ASSOC_CONDITIONS, conditionTypeQName, conditionProperties).getChildRef();
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
    }

    private ConditionsList importConditions(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ConditionsList.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader stringReader = new StringReader(xml);
        return (ConditionsList) jaxbUnmarshaller.unmarshal(stringReader);
    }

    private void processAuthorizedRoles(TPlanItemOnPart planItemOnPart, NodeRef eventRef) {
        if (planItemOnPart.getSourceRef() != null
                && ((TPlanItem) planItemOnPart.getSourceRef()).getDefinitionRef() != null
                && ((TPlanItem) planItemOnPart.getSourceRef()).getDefinitionRef().getClass().equals(TUserEventListener.class)) {
            TUserEventListener userEventListener = (TUserEventListener) ((TPlanItem) planItemOnPart.getSourceRef()).getDefinitionRef();
            List<Object> authorizedRoles = userEventListener.getAuthorizedRoleRefs();
            for (Object role : authorizedRoles) {
                String roleId = ((Role) role).getId();
                NodeRef roleRef = rolesRef.get(roleId);
                nodeService.createAssociation(eventRef, roleRef, EventModel.ASSOC_AUTHORIZED_ROLES);
            }
        }
    }
}
