package ru.citeck.ecos.icase.activity.service.eproc.parser;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.condition.Condition;
import ru.citeck.ecos.cmmn.condition.ConditionProperty;
import ru.citeck.ecos.cmmn.condition.ConditionsList;
import ru.citeck.ecos.cmmn.model.*;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.content.dao.xml.XmlContentDAO;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.model.ActionModel;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseTaskModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CmmnSchemaParser {

    public static final String START_COMPLETENESS_LEVELS_SET_KEY = "startCompletenessLevels";
    public static final String STOP_COMPLETENESS_LEVELS_SET_KEY = "endCompletenessLevels";
    public static final String AUTHORIZED_ROLES_SET_KEY = "authorizedRoles";
    public static final String USER_ACTION_EVENT_TYPE = "user-action";

    private XmlContentDAO<Definitions> xmlContentDAO;
    private DictionaryService dictionaryService;
    private CMMNUtils utils;

    // Parsing execution thread cache
    private ThreadLocal<Map<String, ActivityDefinition>> idToActivityDefinitionCache = new ThreadLocal<>();
    private ThreadLocal<AtomicInteger> triggerDefinitionIdCounter = new ThreadLocal<>();
    private ThreadLocal<AtomicInteger> evaluatorDefinitionIdCounter = new ThreadLocal<>();
    private ThreadLocal<AtomicInteger> activityIndexCounter = new ThreadLocal<>();
    private ThreadLocal<Map<String, String>> idToVarNameRoleCache = new ThreadLocal<>();

    @Autowired
    public CmmnSchemaParser(@Qualifier("caseTemplateContentDAO") XmlContentDAO<Definitions> xmlContentDAO,
                            DictionaryService dictionaryService,
                            CMMNUtils utils) {

        this.xmlContentDAO = xmlContentDAO;
        this.dictionaryService = dictionaryService;
        this.utils = utils;
    }

    public ProcessDefinition parse(byte[] source) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(source)) {
            Definitions definitions = xmlContentDAO.read(stream);
            if (definitions == null || CollectionUtils.isEmpty(definitions.getCase())) {
                return null;
            }
            return parse(definitions.getCase().get(0));
        } catch (IOException e) {
            throw new RuntimeException("Could not parse definition", e);
        }
    }

    public ProcessDefinition parse(Case caseItem) {
        try {
            ProcessDefinition definition = new ProcessDefinition();
            definition.setId(caseItem.getId());
            definition.setRoles(parseRoleDefinitions(caseItem));
            definition.setActivityDefinition(parseRootActivityDefinition(caseItem));
            return definition;
        } finally {
            clearParsingExecutionCache();
        }
    }

    private void clearParsingExecutionCache() {
        idToActivityDefinitionCache.remove();
        triggerDefinitionIdCounter.remove();
        evaluatorDefinitionIdCounter.remove();
        activityIndexCounter.remove();
        idToVarNameRoleCache.remove();
    }

    private ActivityDefinition parseRootActivityDefinition(Case caseItem) {
        ActivityDefinition activityDefinition = newCommonActivityDefinition(caseItem, ActivityType.ROOT);
        activityDefinition.setData(parseCommonDefinitionData(caseItem));
        activityDefinition.setActivities(parseChildActivityDefinitions(caseItem.getCasePlanModel()));
        return activityDefinition;
    }

    private List<ActivityDefinition> parseChildActivityDefinitions(Stage stage) {
        List<JAXBElement<? extends TPlanItemDefinition>> planItemDefinitionList = stage.getPlanItemDefinition();
        if (CollectionUtils.isEmpty(planItemDefinitionList)) {
            return Collections.emptyList();
        }

        List<ActivityDefinition> result = new ArrayList<>(planItemDefinitionList.size());

        for (JAXBElement<? extends TPlanItemDefinition> jaxbPlanItemDefinition : planItemDefinitionList) {
            TPlanItemDefinition planItemDefinition = jaxbPlanItemDefinition.getValue();
            if (utils.isTask(jaxbPlanItemDefinition) || utils.isProcessTask(jaxbPlanItemDefinition)) {
                if (isUserTask((TTask) planItemDefinition)) {
                    result.add(parseUserTask((TTask) planItemDefinition));
                } else if (isAction((TTask) planItemDefinition)) {
                    result.add(parseAction((TTask) planItemDefinition));
                } else {
                    log.warn("Could not parse unknown task: " + ReflectionToStringBuilder.toString(planItemDefinition));
                }
            } else if (utils.isStage(jaxbPlanItemDefinition)) {
                result.add(parseStage((Stage) planItemDefinition));
            } else if (utils.isTimer(jaxbPlanItemDefinition)) {
                result.add(parseTimer((TTimerEventListener) planItemDefinition));
            } else {
                log.warn("Could not parse unknown item: " + ReflectionToStringBuilder.toString(planItemDefinition));
            }
        }

        parseAndAddTransitions(stage);

        return result;
    }

    private ActivityDefinition parseStage(Stage stage) {
        ActivityDefinition activityDefinition = newCommonActivityDefinition(stage, ActivityType.STAGE);
        activityDefinition.setData(parseStageDefinitionData(stage));
        activityDefinition.setActivities(parseChildActivityDefinitions(stage));

        parseAndAddTransitions(stage);

        return activityDefinition;
    }

    private ObjectData parseStageDefinitionData(Stage stage) {
        ObjectData objectData = parseCommonDefinitionData(stage);
        addCompletenessLevels(stage, objectData);
        addAttributeIfExists(stage, CMMNUtils.QNAME_CASE_STATUS, objectData);
        return objectData;
    }

    private boolean isAction(TTask item) {
        QName nodeType = getNodeType(item);
        return dictionaryService.isSubClass(nodeType, ActionModel.TYPE_ACTION);
    }

    private ActivityDefinition parseAction(TTask action) {
        ActivityDefinition activityDefinition = newCommonActivityDefinition(action, ActivityType.ACTION);
        activityDefinition.setData(parseActionDefinitionData(action));
        activityDefinition.setActivities(Collections.emptyList());
        return activityDefinition;
    }

    private ObjectData parseActionDefinitionData(TTask action) {
        ObjectData objectData = parseCommonDefinitionData(action);
        addAttributeIfExists(action, CMMNUtils.QNAME_ACTION_CASE_STATUS, objectData);
        addAttributeIfExists(CmmnDefinitionConstants.ACTION_TYPE, getActionType(action), objectData);
        return objectData;
    }

    private String getActionType(TTask action) {
        QName nodeType = getNodeType(action);
        return nodeType.getLocalName();
    }

    private boolean isUserTask(TTask item) {
        QName nodeType = getNodeType(item);
        return dictionaryService.isSubClass(nodeType, ICaseTaskModel.TYPE_TASK);
    }

    private ActivityDefinition parseUserTask(TTask task) {
        ActivityDefinition activityDefinition = newCommonActivityDefinition(task, ActivityType.USER_TASK);
        activityDefinition.setData(parseTaskDefinitionData(task));
        activityDefinition.setActivities(Collections.emptyList());
        return activityDefinition;
    }

    private ObjectData parseTaskDefinitionData(TTask task) {
        ObjectData objectData = parseCommonDefinitionData(task);
        addRoles(task, objectData);
        addCompletenessLevels(task, objectData);
        return objectData;
    }

    private ActivityDefinition parseTimer(TTimerEventListener timer) {
        ActivityDefinition activityDefinition = newCommonActivityDefinition(timer, ActivityType.TIMER);
        activityDefinition.setData(parseCommonDefinitionData(timer));
        activityDefinition.setActivities(Collections.emptyList());
        return activityDefinition;
    }

    private ActivityDefinition newCommonActivityDefinition(TCmmnElement element, ActivityType activityType) {
        ActivityDefinition activityDefinition = new ActivityDefinition();
        activityDefinition.setId(element.getId());
        activityDefinition.setType(activityType);
        activityDefinition.setIndex(getNextIndex());
        activityDefinition.setRepeatable(isRepeatable(element));

        addActivityDefinitionToIdentityCache(activityDefinition);

        return activityDefinition;
    }

    private boolean isRepeatable(TCmmnElement element) {
        Map<javax.xml.namespace.QName, String> otherAttributes = element.getOtherAttributes();
        javax.xml.namespace.QName qName = utils.convertToXMLQName(ActivityModel.PROP_REPEATABLE);
        return MapUtils.getBoolean(otherAttributes, qName, false);
    }

    private int getNextIndex() {
        return getNextAtomicValue(activityIndexCounter);
    }

    private String getNextTriggerDefinitionId() {
        return "trgr-" + getNextAtomicValue(triggerDefinitionIdCounter);
    }

    private String getNextEvaluatorDefinitionId() {
        return "evltr-" + getNextAtomicValue(evaluatorDefinitionIdCounter);
    }

    private int getNextAtomicValue(ThreadLocal<AtomicInteger> triggerDefinitionIdCounter) {
        AtomicInteger atomicInteger = triggerDefinitionIdCounter.get();
        if (atomicInteger == null) {
            atomicInteger = new AtomicInteger(0);
            triggerDefinitionIdCounter.set(atomicInteger);
        }
        return atomicInteger.incrementAndGet();
    }

    private void addActivityDefinitionToIdentityCache(ActivityDefinition activityDefinition) {
        Map<String, ActivityDefinition> idToActivityDefinitionCacheMap = idToActivityDefinitionCache.get();
        if (idToActivityDefinitionCacheMap == null) {
            idToActivityDefinitionCacheMap = new HashMap<>();
            idToActivityDefinitionCache.set(idToActivityDefinitionCacheMap);
        }

        String id = activityDefinition.getId();
        idToActivityDefinitionCacheMap.put(id, activityDefinition);
    }

    private ActivityDefinition getCachedActivityDefinitionById(String id) {
        Map<String, ActivityDefinition> idToActivityDefinitionCacheMap = idToActivityDefinitionCache.get();
        if (idToActivityDefinitionCacheMap == null) {
            return null;
        }

        return idToActivityDefinitionCacheMap.get(id);
    }

    private ObjectData parseCommonDefinitionData(TCmmnElement element) {
        Map<javax.xml.namespace.QName, String> otherAttributes = element.getOtherAttributes();

        ObjectData data = new ObjectData();
        for (Map.Entry<javax.xml.namespace.QName, String> otherAttribute : otherAttributes.entrySet()) {
            javax.xml.namespace.QName key = otherAttribute.getKey();
            if (!key.getNamespaceURI().equals(CMMNUtils.NAMESPACE)) {
                data.set(key.getLocalPart(), otherAttribute.getValue());
            }
        }

        data.set(CmmnDefinitionConstants.TITLE, otherAttributes.get(CMMNUtils.QNAME_TITLE));

        return data;
    }

    private void addAttributeIfExists(TCmmnElement element, javax.xml.namespace.QName qname, ObjectData objectData) {
        Map<javax.xml.namespace.QName, String> otherAttributes = element.getOtherAttributes();
        String value = otherAttributes.get(qname);
        addAttributeIfExists(qname.getLocalPart(), value, objectData);
    }

    private void addAttributeIfExists(String name, String value, ObjectData objectData) {
        if (StringUtils.isNotBlank(value)) {
            objectData.set(name, value);
        }
    }

    private QName getNodeType(TCmmnElement element) {
        String rawQName = element.getOtherAttributes().get(CMMNUtils.QNAME_NODE_TYPE);
        if (StringUtils.isEmpty(rawQName)) {
            return null;
        }

        return QName.createQName(rawQName);
    }

    // Roles parsing logic area
    private void addRoles(TTask task, ObjectData objectData) {
        Set<String> roleIds = getTaskRoleIds(task);
        Set<String> roleVarNames = convertRoleIdsToVarNames(roleIds);
        objectData.set(CmmnDefinitionConstants.TASK_ROLE_VAR_NAMES_SET_KEY, roleVarNames);
    }

    private Set<String> getTaskRoleIds(TTask task) {
        Set<String> taskRoleIds = new HashSet<>();
        for (Map.Entry<javax.xml.namespace.QName, QName> entry : CMMNUtils.ROLES_ASSOCS_MAPPING.entrySet()) {
            String value = task.getOtherAttributes().get(entry.getKey());
            if (value != null && !value.isEmpty()) {
                String[] rolesArray = value.split(",");
                taskRoleIds.addAll(Arrays.asList(rolesArray));
            }
        }
        return taskRoleIds;
    }

    private Set<String> convertRoleIdsToVarNames(Set<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptySet();
        }

        Set<String> roleVarNames = new HashSet<>();
        for (String roleId : roleIds) {
            String roleVarName = getRoleVarNameByIdFromThreadCache(roleId);
            if (StringUtils.isBlank(roleVarName)) {
                log.warn("RoleVarName not found for id " + roleId);
                continue;
            }
            roleVarNames.add(roleVarName);
        }
        return roleVarNames;
    }

    private List<RoleReference> parseRoleDefinitions(Case caseItem) {
        CaseRoles caseRoles = caseItem.getCaseRoles();
        if (caseRoles == null) {
            return Collections.emptyList();
        }

        List<Role> roles = caseRoles.getRole();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }

        List<RoleReference> result = new ArrayList<>(roles.size());
        for (Role role : roles) {
            RoleReference roleReference = new RoleReference();
            roleReference.setId(role.getId());
            roleReference.setVarName(getRoleVarName(role));
            addRoleReferenceToThreadCache(roleReference);
            result.add(roleReference);
        }

        return result;
    }

    private void addRoleReferenceToThreadCache(RoleReference roleReference) {
        Map<String, String> idToVarNameRoleCacheMap = idToVarNameRoleCache.get();
        if (idToVarNameRoleCacheMap == null) {
            idToVarNameRoleCacheMap = new HashMap<>();
            idToVarNameRoleCache.set(idToVarNameRoleCacheMap);
        }

        idToVarNameRoleCacheMap.put(roleReference.getId(), roleReference.getVarName());
    }

    private String getRoleVarNameByIdFromThreadCache(String roleId) {
        Map<String, String> idToVarNameRoleCacheMap = idToVarNameRoleCache.get();
        if (idToVarNameRoleCacheMap == null) {
            return null;
        }

        return idToVarNameRoleCacheMap.get(roleId);
    }

    private String getRoleVarName(Role role) {
        Map<javax.xml.namespace.QName, String> otherAttributes = role.getOtherAttributes();
        return otherAttributes.get(CMMNUtils.QNAME_ROLE_VARNAME);
    }

    // Completeness Levels parsing logic area
    private void addCompletenessLevels(TCmmnElement definition, ObjectData objectData) {
        Set<String> startCompleteness = parseCompletenessLevels(definition, CMMNUtils.QNAME_START_COMPLETNESS_LEVELS);
        objectData.set(START_COMPLETENESS_LEVELS_SET_KEY, startCompleteness);

        Set<String> stopCompleteness = parseCompletenessLevels(definition, CMMNUtils.QNAME_STOP_COMPLETNESS_LEVELS);
        objectData.set(STOP_COMPLETENESS_LEVELS_SET_KEY, stopCompleteness);
    }

    private Set<String> parseCompletenessLevels(TCmmnElement definition, javax.xml.namespace.QName xmlQName) {
        String rawCompletenessLevels = definition.getOtherAttributes().get(xmlQName);
        if (StringUtils.isNotBlank(rawCompletenessLevels)) {
            String[] splitCompletenessLevels = rawCompletenessLevels.split(",");
            return Arrays.stream(splitCompletenessLevels)
                    .map(this::toValidCompletenessLevelRef)
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private String toValidCompletenessLevelRef(String rawCompletenessLevelRef) {
        return rawCompletenessLevelRef.replaceAll("workspace-SpacesStore-", "workspace://SpacesStore/");
    }

    // Transitions parsing logic area
    private void parseAndAddTransitions(Stage stage) {
        for (TPlanItem planItem : stage.getPlanItem()) {
            TPlanItemDefinition planItemDefinition = (TPlanItemDefinition) planItem.getDefinitionRef();
            String id = planItemDefinition.getId();
            ActivityDefinition activityDefinition = getCachedActivityDefinitionById(id);
            if (activityDefinition == null) {
                log.warn("Activity definition was not fount by id=" + id + ". Events has no been parsed!");
                continue;
            }

            log.debug("Importing events for " + planItem.getId());
            Map<String, Sentry> stageSentries = getSentryIdToInstanceMap(stage);

            getOrCreateTransitionByState(activityDefinition, ActivityState.NOT_STARTED, ActivityState.STARTED);
            getOrCreateTransitionByState(activityDefinition, ActivityState.STARTED, ActivityState.COMPLETED);

            for (Sentry sentry : getEntrySentries(planItem, stageSentries)) {
                addEntryTransition(activityDefinition, sentry);
            }
            for (Sentry sentry : getExitSentries(planItem, stageSentries)) {
                addExitTransition(activityDefinition, sentry);
            }
        }
    }

    private List<Sentry> getEntrySentries(TPlanItem planItem, Map<String, Sentry> stageSentries) {
        List<Sentry> result = new ArrayList<>(utils.criterionToSentries(planItem.getEntryCriterion()));
        String sentriesStr = planItem.getOtherAttributes().get(CMMNUtils.QNAME_ENTRY_SENTRY);
        result.addAll(utils.stringToElements(sentriesStr, stageSentries));
        return result;
    }

    private List<Sentry> getExitSentries(TPlanItem planItem, Map<String, Sentry> stageSentries) {
        List<Sentry> result = new ArrayList<>(utils.criterionToSentries(planItem.getExitCriterion()));
        String sentriesStr = planItem.getOtherAttributes().get(CMMNUtils.QNAME_EXIT_SENTRY);
        result.addAll(utils.stringToElements(sentriesStr, stageSentries));
        return result;
    }

    private Map<String, Sentry> getSentryIdToInstanceMap(Stage stage) {
        Map<String, Sentry> stageSentries = new HashMap<>();
        for (Sentry sentry : stage.getSentry()) {
            stageSentries.put(sentry.getId(), sentry);
        }
        return stageSentries;
    }

    private void addEntryTransition(ActivityDefinition activityDefinition, Sentry sentry) {
        ActivityTransitionDefinition transition = getOrCreateTransitionByState(activityDefinition,
                ActivityState.NOT_STARTED, ActivityState.STARTED);
        TriggerDefinition trigger = getOrCreateTriggerDefinition(transition);
        SentryTriggerDefinition sentryTriggerDefinition = getOrCreateSentryTriggerDefinition(trigger);
        addSentryDefinition(trigger, sentryTriggerDefinition, sentry);

        if (USER_ACTION_EVENT_TYPE.equalsIgnoreCase(getEventType(sentry))) {
            addUserActionAdditionalAttributes(activityDefinition, sentry);
            addUserActionAuthorizedRoles(activityDefinition, sentry);
        }
    }

    private void addUserActionAdditionalAttributes(ActivityDefinition activityDefinition, Sentry sentry) {
        Map<javax.xml.namespace.QName, String> sentryAttributes = sentry.getOtherAttributes();

        for (javax.xml.namespace.QName userActionAttributeQName : CMMNUtils.EVENT_PROPS_MAPPING.keySet()) {
            String attributeValue = sentryAttributes.get(userActionAttributeQName);
            if (attributeValue != null) {
                activityDefinition.getData().set(userActionAttributeQName.getLocalPart(), attributeValue);
            }
        }
    }

    private void addUserActionAuthorizedRoles(ActivityDefinition activityDefinition, Sentry sentry) {
        TPlanItemOnPart planItemOnPart = (TPlanItemOnPart) sentry.getOnPart().get(0).getValue();

        Object sourceRef = planItemOnPart.getSourceRef();
        if (sourceRef == null) {
            return;
        }

        Object definitionRef = ((TPlanItem) sourceRef).getDefinitionRef();
        if (definitionRef != null && definitionRef.getClass().equals(TUserEventListener.class)) {
            TUserEventListener userEventListener = (TUserEventListener) definitionRef;
            List<Object> authorizedRoles = userEventListener.getAuthorizedRoleRefs();

            Set<String> authorizedRoleVarNames = new HashSet<>(authorizedRoles.size());
            for (Object role : authorizedRoles) {
                String roleVarName = getRoleVarName((Role) role);
                authorizedRoleVarNames.add(roleVarName.trim());
            }

            activityDefinition.getData().set(AUTHORIZED_ROLES_SET_KEY, authorizedRoleVarNames);
        }
    }

    private void addExitTransition(ActivityDefinition activityDefinition, Sentry sentry) {
        ActivityTransitionDefinition transition = getOrCreateTransitionByState(activityDefinition,
                ActivityState.STARTED, ActivityState.COMPLETED);
        TriggerDefinition trigger = getOrCreateTriggerDefinition(transition);
        SentryTriggerDefinition sentryTriggerDefinition = getOrCreateSentryTriggerDefinition(trigger);
        addSentryDefinition(trigger, sentryTriggerDefinition, sentry);
    }

    private ActivityTransitionDefinition getOrCreateTransitionByState(ActivityDefinition activityDefinition,
                                                                      ActivityState fromState,
                                                                      ActivityState toState) {
        ActivityTransitionDefinition transition = getTransitionDefinitionByFromAndToState(
                activityDefinition, fromState, toState);
        if (transition == null) {
            transition = new ActivityTransitionDefinition();
            transition.setFromState(fromState);
            transition.setToState(toState);
            transition.setParentActivityDefinition(activityDefinition);

            if (toState == ActivityState.STARTED) {
                //transitionDefinition.setEvaluator(...); // TODO: сделать тут completenessLevel evaluator.
            } else if (toState == ActivityState.COMPLETED) {
                //transitionDefinition.setEvaluator(...); // TODO: сделать тут completenessLevel evaluator.
            }

            if (activityDefinition.getTransitions() == null) {
                activityDefinition.setTransitions(new ArrayList<>());
            }
            activityDefinition.getTransitions().add(transition);
        }
        return transition;
    }

    private ActivityTransitionDefinition getTransitionDefinitionByFromAndToState(ActivityDefinition activityDefinition,
                                                                                 ActivityState fromState,
                                                                                 ActivityState toState) {

        List<ActivityTransitionDefinition> transitions = activityDefinition.getTransitions();
        if (CollectionUtils.isNotEmpty(transitions)) {
            return transitions.stream()
                    .filter(transition -> transition.getFromState() == fromState)
                    .filter(transition -> transition.getToState() == toState)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private TriggerDefinition getOrCreateTriggerDefinition(ActivityTransitionDefinition transition) {
        TriggerDefinition trigger = transition.getTrigger();
        if (trigger == null) {
            trigger = new TriggerDefinition();
            trigger.setId(getNextTriggerDefinitionId());
            trigger.setType(SentryTriggerDefinition.TYPE_NAME);
            trigger.setParentActivityTransitionDefinition(transition);
            transition.setTrigger(trigger);
        }
        return trigger;
    }

    private SentryTriggerDefinition getOrCreateSentryTriggerDefinition(TriggerDefinition trigger) {
        ObjectData data = trigger.getData();
        if (data == null) {
            SentryTriggerDefinition sentryTriggerDefinition = new SentryTriggerDefinition();
            data = new ObjectData(sentryTriggerDefinition);
            trigger.setData(data);
        }
        return data.getAs(SentryTriggerDefinition.class);
    }

    private void addSentryDefinition(TriggerDefinition trigger, SentryTriggerDefinition sentryTriggerDefinition,
                                     Sentry sentry) {

        SentryDefinition sentryDefinition = new SentryDefinition();
        sentryDefinition.setId(sentry.getId());
        sentryDefinition.setEvent(getEventType(sentry));
        sentryDefinition.setSourceRef(getSourceRef(sentry));
        sentryDefinition.setEvaluator(composeEvaluatorDefinition(sentry));
        sentryDefinition.setParentTriggerDefinition(trigger);

        List<SentryDefinition> sentries = sentryTriggerDefinition.getSentries();
        if (sentries == null) {
            sentries = new ArrayList<>();
            sentryTriggerDefinition.setSentries(sentries);
        }

        sentries.add(sentryDefinition);
    }

    private String getEventType(Sentry sentry) {
        return sentry.getOtherAttributes().get(CMMNUtils.QNAME_ORIGINAL_EVENT);
    }

    private SourceRef getSourceRef(Sentry sentry) {
        List<JAXBElement<? extends TOnPart>> onParts = sentry.getOnPart();
        JAXBElement<? extends TOnPart> onPart = onParts.get(0);
        String sourceId = onPart.getValue().getOtherAttributes().get(CMMNUtils.QNAME_SOURCE_ID);

        SourceRef sourceRef = new SourceRef();
        sourceRef.setRef(sourceId);
        return sourceRef;
    }

    private EvaluatorDefinition composeEvaluatorDefinition(Sentry sentry) {
        if (sentry.getIfPart() == null) {
            return null;
        }

        TIfPart ifPart = sentry.getIfPart();
        TExpression expression = ifPart.getCondition();
        String content = (String) expression.getContent().get(0);
        content = content.replace("<!CDATA[", "").replace("]]>", "");
        try {
            List<Condition> conditions = unmarshalConditions(content).getConditions();
            return composeGroupEvaluatorDefinition(conditions);
        } catch (JAXBException e) {
            throw new RuntimeException("Error of parsing condition of sentry " + sentry.getId(), e);
        }
    }

    private EvaluatorDefinition composeGroupEvaluatorDefinition(List<Condition> conditions) {
        EvaluatorDefinition definition = new EvaluatorDefinition();
        definition.setId(getNextEvaluatorDefinitionId());
        definition.setInverse(false);
        definition.setData(new ObjectData(composeEvaluatorDefinitionData(conditions)));
        return definition;
    }

    private List<EvaluatorDefinitionData> composeEvaluatorDefinitionData(List<Condition> conditions) {
        List<EvaluatorDefinitionData> result = new ArrayList<>();
        for (Condition condition : conditions) {
            EvaluatorDefinitionData definitionData = new EvaluatorDefinitionData();

            QName type = utils.convertFromXMLQName(condition.getType());
            definitionData.setType(type.getLocalName());

            Map<String, String> evaluatorProperties = new HashMap<>(condition.getProperties().size());
            for (ConditionProperty property : condition.getProperties()) {
                String propertyName = utils.convertFromXMLQName(property.getType()).getLocalName();
                String propertyValue = property.getValue();
                evaluatorProperties.put(propertyName, propertyValue);
            }
            definitionData.setAttributes(evaluatorProperties);

            result.add(definitionData);
        }
        return result;
    }

    private ConditionsList unmarshalConditions(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ConditionsList.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader stringReader = new StringReader(xml);
        return (ConditionsList) jaxbUnmarshaller.unmarshal(stringReader);
    }

}
