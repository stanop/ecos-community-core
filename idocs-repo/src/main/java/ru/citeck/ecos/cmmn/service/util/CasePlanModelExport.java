package ru.citeck.ecos.cmmn.service.util;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.model.*;
import ru.citeck.ecos.cmmn.service.CaseExportService;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.*;
import ru.citeck.ecos.utils.RepoUtils;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.*;

/**
 * @author deathNC
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 * @author Pavel Simonov (pavel.simonov@citeck.ru)
 */
public class CasePlanModelExport {
    private static final Logger logger = Logger.getLogger(CasePlanModelExport.class);

    private final NodeService nodeService;
    private final DictionaryService dictionaryService;
    private final CaseExportService caseExportService;
    private final CaseActivityService caseActivityService;
    private final static Map<String, PlanItemTransition> ACTIVITY_EVENT_TYPES_MAPPING = new HashMap<>();
    private final static String USER_ACTION_TYPE = "user-action";
    private CaseRoles caseRoles;
    /*
     * Mapping of real nodeRef id's and generated id's for planItems
     * key - nodeRef, value - planItem's ID
     */
    private Map<String, String> nodeRefsToPlanItemsMap = new HashMap<>();
    /*
     * Map contains created planItem instances accessible by it ID
     */
    private Map<String, TPlanItem> createdPlanItems = new HashMap<>();

    static {
        ACTIVITY_EVENT_TYPES_MAPPING.put(USER_ACTION_TYPE, PlanItemTransition.OCCUR);
        ACTIVITY_EVENT_TYPES_MAPPING.put(ICaseEventModel.CONSTR_ACTIVITY_STARTED, PlanItemTransition.START);
        ACTIVITY_EVENT_TYPES_MAPPING.put(ICaseEventModel.CONSTR_STAGE_CHILDREN_STOPPED, PlanItemTransition.COMPLETE);
        ACTIVITY_EVENT_TYPES_MAPPING.put(ICaseEventModel.CONSTR_ACTIVITY_STOPPED, PlanItemTransition.COMPLETE);
        ACTIVITY_EVENT_TYPES_MAPPING.put(ICaseEventModel.CONSTR_CASE_CREATED, PlanItemTransition.CREATE);
        ACTIVITY_EVENT_TYPES_MAPPING.put(ICaseEventModel.CONSTR_CASE_PROPERTIES_CHANGED, PlanItemTransition.RESUME);//TODO: IT'S MUST BE CASE FILE TRANSITION 'update'
    }

    public CasePlanModelExport(NodeService nodeService, CaseActivityService caseActivityService, CaseExportService caseExportService, DictionaryService dictionaryService) {
        this.nodeService = nodeService;
        this.dictionaryService = dictionaryService;
        this.caseExportService = caseExportService;
        this.caseActivityService = caseActivityService;
    }

    public Stage getCasePlanModel(NodeRef caseNodeRef, CaseRoles caseRoles) {
        this.caseRoles = caseRoles;
        Stage casePlanModel = createCasePlanModel();
        if (nodeService.exists(caseNodeRef)) {
            fillCasePlanModel(casePlanModel, caseNodeRef);
        }
        return casePlanModel;
    }

    private void fillCasePlanModel(Stage casePlanModel, NodeRef caseNodeRef) {
        NodeRef caseRootRef = caseExportService.getElementTypeByConfig(caseNodeRef, ActivityModel.TYPE_ACTIVITY);
        nodeRefsToPlanItemsMap.put(CMMNUtils.convertNodeRefToId(caseNodeRef), casePlanModel.getId());
        exportCompletnessLevels(casePlanModel, caseNodeRef);
        if (caseRootRef != null) {
            exportActivityNode(casePlanModel,
                    caseActivityService.getActivities(caseRootRef, ICaseTemplateModel.ASSOC_INTERNAL_ELEMENTS, RegexQNamePattern.MATCH_ALL));
        }
        fixSentriesIds(casePlanModel);
    }

    private void exportActivityNode(Stage parentStage, List<NodeRef> activitiesRef) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exporting parent activities");
        }
        for (NodeRef activityRef : activitiesRef) {
            if (isStage(activityRef)) {
                Stage stage = toStage(activityRef);
                TPlanItem planItem = getPlanItem(stage);
                parentStage.getPlanItem().add(planItem);
                exportActivityNode(stage, caseActivityService.getActivities(activityRef));
                parentStage.getPlanItemDefinition().add(caseExportService.getObjectFactory().createStage(stage));
                parentStage.getSentry().addAll(exportSentries(activityRef, planItem, parentStage));
                processCompletnessLevels(activityRef, stage);
            } else if (isAction(activityRef)) {
                TTask task = getTask(activityRef);
                TPlanItem planItem = getPlanItem(task);
                parentStage.getPlanItem().add(planItem);
                parentStage.getPlanItemDefinition().add(caseExportService.getObjectFactory().createTask(task));
                parentStage.getSentry().addAll(exportSentries(activityRef, planItem, parentStage));
                processCompletnessLevels(activityRef, task);
            } else if (isTask(activityRef)) {
                TProcessTask task = getProcessTask(activityRef);
                TPlanItem planItem = getPlanItem(task);
                parentStage.getPlanItem().add(planItem);
                parentStage.getPlanItemDefinition().add(caseExportService.getObjectFactory().createProcessTask(task));
                parentStage.getSentry().addAll(exportSentries(activityRef, planItem, parentStage));
                processCompletnessLevels(activityRef, task);
            } else if (isTimer(activityRef)) {
                TTimerEventListener timer = getTimerListener(activityRef);
                TPlanItem planItem = getPlanItem(timer);
                parentStage.getPlanItem().add(planItem);
                parentStage.getPlanItemDefinition().add(caseExportService.getObjectFactory().createTimerEventListener(timer));
                parentStage.getSentry().addAll(exportSentries(activityRef, planItem, parentStage));
                //move entry and exit criterion to other attributes because standard not allow it for timer
                moveCriterionToAttributes(planItem);
            }
        }
    }

    private void moveCriterionToAttributes(TPlanItem planItem) {
        Map<QName, String> attr = planItem.getOtherAttributes();
        List<Sentry> entrySentry = CMMNUtils.criterionToSentries(planItem.getEntryCriterion());
        attr.put(CMMNUtils.QNAME_ENTRY_SENTRY, CMMNUtils.elementsToString(entrySentry));
        planItem.getEntryCriterion().clear();
        List<Sentry> exitSentry = CMMNUtils.criterionToSentries(planItem.getExitCriterion());
        attr.put(CMMNUtils.QNAME_EXIT_SENTRY, CMMNUtils.elementsToString(exitSentry));
        planItem.getExitCriterion().clear();
    }

    private void exportCompletnessLevels(Stage casePlanModel, NodeRef caseNodeRef) {
        NodeRef caseCompletnessLevelConfig = caseExportService.getElementTypeByConfig(caseNodeRef, RequirementModel.TYPE_COMPLETENESS_LEVEL);
        if (caseCompletnessLevelConfig != null) {
            List<NodeRef> nodeRefs = RepoUtils.getTargetAssoc(caseCompletnessLevelConfig, ICaseTemplateModel.ASSOC_EXTERNAL_ELEMENTS, nodeService);
            if (nodeRefs != null && !nodeRefs.isEmpty()) {
                StringBuilder refs = new StringBuilder();
                for (NodeRef nodeRef : nodeRefs) {
                    if (refs.length() != 0) {
                        refs.append(",");
                    }
                    refs.append(CMMNUtils.extractIdFromNodeRef(nodeRef));
                }
                casePlanModel.getOtherAttributes().put(CMMNUtils.QNAME_COMPLETNESS_LEVELS, refs.toString());
            }
        }
    }

    private Stage toStage(NodeRef nodeRef) {
        Stage stage = new Stage();
        stage.setId(CMMNUtils.convertNodeRefToId(nodeRef));
        stage.setAutoComplete(isStageAutoCompleted(nodeRef));
        saveAttributes(stage, nodeRef);
        return stage;
    }

    private TTask getTask(NodeRef activityRef) {
        TTask task = new TTask();
        task.setIsBlocking(false);
        task.setId(CMMNUtils.convertNodeRefToId(activityRef));
        saveType(task, activityRef);
        saveAttributes(task, activityRef);
        return task;
    }

    private TTimerEventListener getTimerListener(NodeRef activityRef) {
        TTimerEventListener timer = new TTimerEventListener();
        timer.setId(CMMNUtils.convertNodeRefToId(activityRef));
        saveType(timer, activityRef);
        saveAttributes(timer, activityRef);
        return timer;
    }

    private TProcessTask getProcessTask(NodeRef activityRef) {
        TProcessTask task = new TProcessTask();
        task.setId(CMMNUtils.convertNodeRefToId(activityRef));
        task.setIsBlocking(true);
        saveType(task, activityRef);
        saveAttributes(task, activityRef);
        return task;
    }

    private TPlanItem getPlanItem(TPlanItemDefinition definition) {
        TPlanItem planItem = new TPlanItem();
        planItem.setId(CMMNUtils.getNextDocumentId());
        planItem.setDefinitionRef(definition);
        nodeRefsToPlanItemsMap.put(definition.getId(), planItem.getId());
        createdPlanItems.put(planItem.getId(), planItem);
        return planItem;
    }

    private List<Sentry> exportSentries(NodeRef activityRef, TPlanItem planItem, Stage parentStage) {

        List<Sentry> sentries = new ArrayList<>();

        for (NodeRef eventRef : getEntryEvents(activityRef)) {
            Sentry sentry = createSentry(parentStage, eventRef);
            TEntryCriterion entryCriterion = new TEntryCriterion();
            entryCriterion.setSentryRef(sentry);
            entryCriterion.setId(CMMNUtils.getNextDocumentId());
            planItem.getEntryCriterion().add(entryCriterion);
            sentries.add(sentry);
        }

        for (NodeRef eventRef : getExitEvents(activityRef)) {
            Sentry sentry = createSentry(parentStage, eventRef);
            TExitCriterion exitCriterion = new TExitCriterion();
            exitCriterion.setSentryRef(sentry);
            exitCriterion.setId(CMMNUtils.getNextDocumentId());
            planItem.getExitCriterion().add(exitCriterion);
            sentries.add(sentry);
        }

        return sentries;
    }

    private List<NodeRef> getEntryEvents(NodeRef activityRef) {
        List<NodeRef> events = new ArrayList<>();
        events.addAll(RepoUtils.getChildrenByAssoc(activityRef, ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS, nodeService));
        events.addAll(RepoUtils.getChildrenByAssoc(activityRef, ICaseEventModel.ASSOC_ACTIVITY_RESTART_EVENTS, nodeService));
        return events;
    }

    private List<NodeRef> getExitEvents(NodeRef activityRef) {
        org.alfresco.service.namespace.QName type = nodeService.getType(activityRef);
        if (type.equals(CaseTimerModel.TYPE_TIMER)) {
            return RepoUtils.getChildrenByAssoc(activityRef, ICaseEventModel.ASSOC_ACTIVITY_RESET_EVENTS, nodeService);
        } else {
            return RepoUtils.getChildrenByAssoc(activityRef, ICaseEventModel.ASSOC_ACTIVITY_END_EVENTS, nodeService);
        }
    }

    private String getEventSourceId(Stage parentStage, NodeRef eventRef) {

        String eventType = (String) nodeService.getProperty(eventRef, EventModel.PROP_TYPE);

        if (eventType.equals(USER_ACTION_TYPE)) {
            TUserEventListener userEventListener = new TUserEventListener();
            userEventListener.setId(CMMNUtils.getNextDocumentId());
            List<NodeRef> authorizedRolesRef = RepoUtils.getTargetNodeRefs(eventRef, EventModel.ASSOC_AUTHORIZED_ROLES, nodeService);
            for (NodeRef authorizedRoleRef : authorizedRolesRef) {
                for (Role role : caseRoles.getRole()) {
                    if (role.getId().equals(CMMNUtils.convertNodeRefToId(authorizedRoleRef))) {
                        userEventListener.getAuthorizedRoleRefs().add(role);
                    }
                }
            }
            TPlanItem userEventPlanItem = getPlanItem(userEventListener);
            nodeRefsToPlanItemsMap.put(userEventPlanItem.getId(), userEventPlanItem.getId());
            parentStage.getPlanItem().add(userEventPlanItem);
            parentStage.getPlanItemDefinition().add(caseExportService.getObjectFactory().createUserEventListener(userEventListener));
            return userEventPlanItem.getId();
        } else {
            NodeRef sourceRef = RepoUtils.getFirstTargetAssoc(eventRef, EventModel.ASSOC_EVENT_SOURCE, nodeService);
            return CMMNUtils.convertNodeRefToId(sourceRef);
        }
    }

    private TPlanItemOnPart createPlanItemOnPart(Stage parentStage, NodeRef eventRef) {

        TPlanItemOnPart planItemOnPart = new TPlanItemOnPart();
        planItemOnPart.setId(CMMNUtils.getNextDocumentId());
        NodeRef eventSourceRef = RepoUtils.getFirstTargetAssoc(eventRef, EventModel.ASSOC_EVENT_SOURCE, nodeService);
        saveNodeAttribute(planItemOnPart, CMMNUtils.QNAME_TITLE, eventSourceRef, ContentModel.PROP_TITLE);
        planItemOnPart.setStandardEvent(getPlanItemTransition(eventRef));
        planItemOnPart.getOtherAttributes().put(CMMNUtils.QNAME_SOURCE_ID, CMMNUtils.convertNodeRefToId(eventSourceRef));
        planItemOnPart.getOtherAttributes().put(CMMNUtils.QNAME_NODE_TYPE, nodeService.getType(eventRef).toString());
        planItemOnPart.setSourceRef(getEventSourceId(parentStage, eventRef));

        if (nodeService.getProperty(eventRef, ContentModel.PROP_TITLE) != null) {
            planItemOnPart.getOtherAttributes().put(CMMNUtils.QNAME_TITLE, (String) nodeService.getProperty(eventRef, ContentModel.PROP_TITLE));
        }

        org.alfresco.service.namespace.QName parentAssocType = nodeService.getPrimaryParent(eventRef).getTypeQName();
        if (ICaseEventModel.ASSOC_ACTIVITY_RESTART_EVENTS.equals(parentAssocType)) {
            planItemOnPart.getOtherAttributes().put(CMMNUtils.QNAME_IS_RESTART_EVENT, "true");
        }

        return planItemOnPart;
    }

    private TIfPart getIfPart(NodeRef nodeRef) {
        List<NodeRef> conditionsRef = RepoUtils.getChildrenByAssoc(nodeRef, EventModel.ASSOC_CONDITIONS, nodeService);
        if (!conditionsRef.isEmpty()) {
            TIfPart ifPart = new TIfPart();
            CaseConditionExporter caseConditionExporter = new CaseConditionExporter(conditionsRef, nodeService);
            try {
                String conditions = caseConditionExporter.generateXML();
                TExpression expression = new TExpression();
                expression.getContent().add("<!CDATA[" + conditions + "]]>");
                ifPart.setCondition(expression);
            } catch (JAXBException e) {
                logger.error(e);
            }
            return ifPart;
        }
        return null;
    }

    private PlanItemTransition getPlanItemTransition(NodeRef eventRef) {

        String eventType = (String) nodeService.getProperty(eventRef, EventModel.PROP_TYPE);
        NodeRef source = RepoUtils.getFirstTargetAssoc(eventRef, EventModel.ASSOC_EVENT_SOURCE, nodeService);
        org.alfresco.service.namespace.QName sourceType = nodeService.getType(source);

        if (eventType.equals(ICaseEventModel.CONSTR_ACTIVITY_STOPPED)
                     && sourceType.equals(CaseTimerModel.TYPE_TIMER)) {
            return PlanItemTransition.OCCUR;
        } else {
            return ACTIVITY_EVENT_TYPES_MAPPING.get(eventType);
        }
    }

    private Sentry createSentry(Stage parentStage, NodeRef eventRef) {
        String eventType = (String) nodeService.getProperty(eventRef, EventModel.PROP_TYPE);

        TPlanItemOnPart planItemOnPart = createPlanItemOnPart(parentStage, eventRef);
        TIfPart ifPart = getIfPart(eventRef);
        Sentry sentry = new Sentry();
        sentry.setId(CMMNUtils.getNextDocumentId());
        sentry.getOtherAttributes().put(CMMNUtils.QNAME_ORIGINAL_EVENT, eventType);
        sentry.getOnPart().add(caseExportService.getObjectFactory().createPlanItemOnPart(planItemOnPart));
        if (ifPart != null) {
            sentry.setIfPart(ifPart);
        }
        for (Map.Entry<QName, org.alfresco.service.namespace.QName> entry : CMMNUtils.EVENT_PROPS_MAPPING.entrySet()) {
            String value = CMMNUtils.convertValueForCmmn(entry.getValue(), nodeService.getProperty(eventRef, entry.getValue()));
            if (value != null) {
                sentry.getOtherAttributes().put(entry.getKey(), value);
            }
        }
        return sentry;
    }

    private void processCompletnessLevels(NodeRef activityRef, TPlanItemDefinition planItem) {
        List<NodeRef> startCompletnessLevelRefs = RepoUtils.getTargetNodeRefs(activityRef, StagesModel.ASSOC_START_COMPLETENESS_LEVELS_RESTRICTION, nodeService);
        List<NodeRef> stopCompletnessLevelRefs = RepoUtils.getTargetNodeRefs(activityRef, StagesModel.ASSOC_STOP_COMPLETENESS_LEVELS_RESTRICTION, nodeService);
        StringBuilder sb = new StringBuilder();
        for (NodeRef ref : startCompletnessLevelRefs) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(CMMNUtils.extractIdFromNodeRef(ref));
        }
        planItem.getOtherAttributes().put(CMMNUtils.QNAME_START_COMPLETNESS_LEVELS, sb.toString());
        sb = new StringBuilder();
        for (NodeRef ref : stopCompletnessLevelRefs) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(CMMNUtils.extractIdFromNodeRef(ref));
        }
        planItem.getOtherAttributes().put(CMMNUtils.QNAME_STOP_COMPLETNESS_LEVELS, sb.toString());
    }

    private void fixSentriesIds(Stage casePlanModel) {
        List<Sentry> sentries = casePlanModel.getSentry();
        for (Sentry sentry : sentries) {
            List<JAXBElement<? extends TOnPart>> onParts = sentry.getOnPart();
            for (JAXBElement<? extends TOnPart> value : onParts) {
                String id = (String) ((TPlanItemOnPart) value.getValue()).getSourceRef();
                if (nodeRefsToPlanItemsMap.containsKey(id)) {
                    ((TPlanItemOnPart) value.getValue()).setSourceRef(createdPlanItems.get(nodeRefsToPlanItemsMap.get(id)));
                } else {
                    logger.warn("Can't find planItem for id: " + id);
                }
            }
        }
        if (!casePlanModel.getPlanItemDefinition().isEmpty()) {
            for (JAXBElement<? extends TPlanItemDefinition> planItemDefinition : casePlanModel.getPlanItemDefinition()) {
                if (planItemDefinition.getValue().getClass().equals(Stage.class)) {
                    fixSentriesIds((Stage) planItemDefinition.getValue());
                }
            }
        }
    }

    private boolean isStageAutoCompleted(NodeRef nodeRef) {
        return !((Boolean) nodeService.getProperty(nodeRef, ActivityModel.PROP_MANUAL_STOPPED));
    }

    private boolean isAction(NodeRef nodeRef) {
        org.alfresco.service.namespace.QName nodeType = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(nodeType, ActionModel.TYPE_ACTION);
    }

    private boolean isTask(NodeRef nodeRef) {
        org.alfresco.service.namespace.QName nodeType = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(nodeType, ICaseTaskModel.TYPE_TASK);
    }

    private boolean isStage(NodeRef caseNodeRef) {
        return nodeService.getType(caseNodeRef).equals(StagesModel.TYPE_STAGE);
    }

    private boolean isTimer(NodeRef nodeRef) {
        return nodeService.getType(nodeRef).equals(CaseTimerModel.TYPE_TIMER);
    }

    private Stage createCasePlanModel() {
        Stage stage = new Stage();
        stage.setId(CMMNUtils.getNextDocumentId());
        stage.setAutoComplete(true);
        stage.setName("Case plan model");
        return stage;
    }

    private void saveType(TPlanItemDefinition definition, NodeRef sourceRef) {
        definition.getOtherAttributes().put(CMMNUtils.QNAME_NODE_TYPE, nodeService.getType(sourceRef).toString());
    }

    private void saveAttributes(TPlanItemDefinition definition, NodeRef sourceRef) {

        for (Map.Entry<QName, org.alfresco.service.namespace.QName> entry : CMMNUtils.ROLES_ASSOCS_MAPPING.entrySet()) {
            String assocs = rolesToStringList(RepoUtils.getTargetAssoc(sourceRef, entry.getValue(), nodeService));
            if (!assocs.isEmpty()) {
                definition.getOtherAttributes().put(entry.getKey(), assocs);
            }
        }

        for (Map.Entry<QName, org.alfresco.service.namespace.QName> entry : CMMNUtils.STATUS_ASSOCS_MAPPING.entrySet()) {
            NodeRef statusRef = RepoUtils.getFirstTargetAssoc(sourceRef, entry.getValue(), nodeService);
            if (statusRef != null) {
                String statusName = (String) nodeService.getProperty(statusRef, ContentModel.PROP_NAME);
                definition.getOtherAttributes().put(entry.getKey(), statusName);
            }
        }

        definition.setName(nodeService.getProperty(sourceRef, ContentModel.PROP_NAME).toString());
        saveNodeAttribute(definition, CMMNUtils.QNAME_TITLE, sourceRef, ContentModel.PROP_TITLE);
        saveNodeAttribute(definition, CMMNUtils.QNAME_DESCRIPTION, sourceRef, ContentModel.PROP_DESCRIPTION);
        saveTypeProperties(definition, sourceRef);
    }

    private void saveTypeProperties(TCmmnElement element, NodeRef sourceRef) {
        Map<org.alfresco.service.namespace.QName, Serializable> properties = nodeService.getProperties(sourceRef);
        for (org.alfresco.service.namespace.QName key : properties.keySet()) {
            if (!key.getNamespaceURI().equals("http://www.alfresco.org/model/system/1.0") &&
                    !key.getNamespaceURI().equals("http://www.alfresco.org/model/content/1.0")) {
                QName qName = CMMNUtils.convertToXMLQName(key);
                if (!qName.getLocalPart().contains("_added")) {
                    if (properties.get(key) != null) {
                        if (properties.get(key).getClass().equals(Date.class)) {
                            element.getOtherAttributes().put(qName, ISO8601DateFormat.format((Date) properties.get(key)));
                        } else {
                            element.getOtherAttributes().put(qName, properties.get(key).toString());
                        }
                    } else {
                        element.getOtherAttributes().put(qName, "");
                    }
                }
            }
        }
    }

    private void saveNodeAttribute(TCmmnElement element, QName attrQName, NodeRef nodeRef, org.alfresco.service.namespace.QName qName) {
        if (nodeService.getProperty(nodeRef, qName) != null) {
            element.getOtherAttributes().put(attrQName, nodeService.getProperty(nodeRef, qName).toString());
        }
    }

    private String rolesToStringList(List<NodeRef> rolesRef) {
        StringBuilder sbRoles = new StringBuilder();
        if (!rolesRef.isEmpty()) {
            for (NodeRef roleRef : rolesRef) {
                if (sbRoles.length() > 0) {
                    sbRoles.append(",");
                }
                sbRoles.append(CMMNUtils.convertNodeRefToId(roleRef));
            }
        }
        return sbRoles.toString();
    }
}
