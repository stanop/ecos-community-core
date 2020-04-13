package ru.citeck.ecos.icase.activity.service.eproc;

import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.Data;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.commands.dto.CommandResult;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.commons.utils.MandatoryParam;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.eproc.commands.*;
import ru.citeck.ecos.icase.activity.service.eproc.commands.response.*;
import ru.citeck.ecos.icase.activity.service.eproc.parser.CmmnSchemaParser;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.node.EcosTypeService;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EProcActivityServiceImpl implements EProcActivityService {

    private static final String CMMN_PROCESS_TYPE = "cmmn";
    private static final String EPROC_TARGET_APP_NAME = "eproc";
    private static final String EPROC_SAVE_STATE_TRANSACTION_KEY = EProcActivityServiceImpl.class.getName() + ".save-state";
    private static final String EPROC_CASE_STATE_BY_ID_KEY_PREFIX = "eproc-case-state-by-id";

    private CmmnSchemaParser cmmnSchemaParser;
    private CommandsService commandsService;
    private EcosTypeService ecosTypeService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;

    private LoadingCache<EcosAlfTypesKey, String> typesToRevisionIdCache;
    private LoadingCache<String, ProcessDefinition> revisionIdToProcessDefinitionCache;

    @Autowired
    public EProcActivityServiceImpl(CmmnSchemaParser cmmnSchemaParser,
                                    CommandsService commandsService,
                                    EcosTypeService ecosTypeService,
                                    DictionaryService dictionaryService,
                                    NodeService nodeService) {

        this.commandsService = commandsService;
        this.ecosTypeService = ecosTypeService;
        this.cmmnSchemaParser = cmmnSchemaParser;
        this.dictionaryService = dictionaryService;
        this.nodeService = nodeService;

        this.typesToRevisionIdCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(CacheLoader.from(this::findProcDefRevIdFromMicroservice));

        this.revisionIdToProcessDefinitionCache = CacheBuilder.newBuilder()
                .maximumSize(120)
                .build(CacheLoader.from(this::getProcessDefinitionByRevisionIdFromMicroservice));
    }

    @Override
    public Pair<String, byte[]> getRawDefinitionForType(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);
        String processRevisionId = getRevisionIdForNode(caseNodeRef);

        GetProcDefRevResp result = getProcessDefinitionByRevisionIdFromMicroserviceImpl(processRevisionId);
        if (result == null) {
            return null;
        }
        return new Pair<>(processRevisionId, result.getData());
    }

    @Override
    public ProcessDefinition getFullDefinition(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        String stateId = (String) nodeService.getProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID);
        if (StringUtils.isNotBlank(stateId)) {
            return getFullDefinitionForExisting(stateId);
        } else {
            return getFullDefinitionForNewCase(caseNodeRef);
        }
    }

    private ProcessDefinition getFullDefinitionForExisting(String stateId) {
        GetProcStateResp processState = getProcessStateFromMicroservice(stateId);
        if (processState == null) {
            throw new IllegalArgumentException("Can not find state for stateId=" + stateId);
        }

        String procDefRevId = processState.getProcDefRevId();
        return revisionIdToProcessDefinitionCache.getUnchecked(procDefRevId);
    }

    private ProcessDefinition getFullDefinitionForNewCase(NodeRef caseNodeRef) {
        String processRevisionId = getRevisionIdForNode(caseNodeRef);
        return revisionIdToProcessDefinitionCache.getUnchecked(processRevisionId);
    }

    private String getRevisionIdForNode(NodeRef caseNodeRef) {
        EcosAlfTypesKey ecosAlfTypesKey = composeEcosAlfTypesKey(caseNodeRef);
        String processRevisionId = typesToRevisionIdCache.getUnchecked(ecosAlfTypesKey);
        if (StringUtils.isBlank(processRevisionId)) {
            throw new RuntimeException("Can not find processRevisionId for caseRef=" + caseNodeRef);
        }
        return processRevisionId;
    }

    private EcosAlfTypesKey composeEcosAlfTypesKey(NodeRef caseNodeRef) {
        RecordRef ecosType = ecosTypeService.getEcosType(caseNodeRef);

        List<QName> alfQNameTypes = getTypeInheritanceListForCase(caseNodeRef);
        List<String> alfTypes = toString(alfQNameTypes);

        return new EcosAlfTypesKey(ecosType, alfTypes);
    }

    private List<QName> getTypeInheritanceListForCase(NodeRef caseNodeRef) {
        QName type = nodeService.getType(caseNodeRef);
        TypeDefinition typeDef = dictionaryService.getType(type);

        List<QName> result = new ArrayList<>();
        while (typeDef != null) {
            result.add(typeDef.getName());
            QName parentTypeQName = typeDef.getParentName();
            if (parentTypeQName == null) {
                typeDef = null;
                continue;
            }
            typeDef = dictionaryService.getType(parentTypeQName);
        }

        return result;
    }

    private List<String> toString(List<QName> alfQNames) {
        if (CollectionUtils.isEmpty(alfQNames)) {
            return Collections.emptyList();
        }

        return alfQNames.stream()
                .map(QName::toString)
                .collect(Collectors.toList());
    }

    private String findProcDefRevIdFromMicroservice(EcosAlfTypesKey key) {
        FindProcDef findProcDefCommand = new FindProcDef();
        findProcDefCommand.setProcType(CMMN_PROCESS_TYPE);
        findProcDefCommand.setEcosTypeRef(key.getEcosType());
        findProcDefCommand.setAlfTypes(key.getAlfTypes());

        CommandResult commandResult = commandsService.executeRemoteSync(findProcDefCommand, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while find of process definition. " +
                    "For detailed information see logs");
        }

        FindProcDefResp response = commandResult.getCommandAs(FindProcDefResp.class);
        if (response == null) {
            return null;
        }
        return response.getProcDefRevId();
    }

    private ProcessDefinition getProcessDefinitionByRevisionIdFromMicroservice(String definitionRevisionId) {
        GetProcDefRevResp response = getProcessDefinitionByRevisionIdFromMicroserviceImpl(definitionRevisionId);
        if (response == null) {
            return null;
        }
        return cmmnSchemaParser.parse(response.getData());
    }

    private GetProcDefRevResp getProcessDefinitionByRevisionIdFromMicroserviceImpl(String definitionRevisionId) {
        GetProcDefRev getProcDefRevCommand = new GetProcDefRev();
        getProcDefRevCommand.setProcType(CMMN_PROCESS_TYPE);
        getProcDefRevCommand.setProcDefRevId(definitionRevisionId);

        CommandResult commandResult = commandsService.executeRemoteSync(getProcDefRevCommand, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while receiving of process definition revision. " +
                    "For detailed information see logs");
        }

        return commandResult.getCommandAs(GetProcDefRevResp.class);
    }

    @Override
    public ProcessInstance createDefaultState(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        EcosAlfTypesKey ecosAlfTypesKey = composeEcosAlfTypesKey(caseNodeRef);
        String definitionRevisionId = typesToRevisionIdCache.getUnchecked(ecosAlfTypesKey);
        CreateProcResp createProcResp = createProcessInstanceInMicroservice(definitionRevisionId, caseRef);
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_PROCESS_ID, createProcResp.getProcId());
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID, createProcResp.getProcStateId());

        ProcessDefinition definition = getFullDefinitionForNewCase(caseNodeRef);
        ProcessInstance processInstance = createProcessInstanceFromDefinition(
                createProcResp.getProcId(),
                caseRef,
                definition);

        putInstanceToTransactionScopeByStateId(caseRef, processInstance);

        return processInstance;
    }

    @Override
    public ProcessInstance createDefaultState(RecordRef caseRef, String revisionId, ProcessDefinition definition) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        CreateProcResp createProcResp = createProcessInstanceInMicroservice(revisionId, caseRef);
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_PROCESS_ID, createProcResp.getProcId());
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID, createProcResp.getProcStateId());

        ProcessInstance processInstance = createProcessInstanceFromDefinition(
                createProcResp.getProcId(),
                caseRef,
                definition);

        putInstanceToTransactionScopeByStateId(caseRef, processInstance);

        return processInstance;
    }

    private CreateProcResp createProcessInstanceInMicroservice(String definitionRevisionId, RecordRef caseRef) {
        CreateProc createProc = new CreateProc();
        createProc.setProcDefRevId(definitionRevisionId);
        createProc.setRecordRef(caseRef);

        CommandResult commandResult = commandsService.executeRemoteSync(createProc, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while creation of process state. For detailed information see logs");
        }
        return commandResult.getCommandAs(CreateProcResp.class);
    }

    private ProcessInstance createProcessInstanceFromDefinition(String procId,
                                                                RecordRef caseRef,
                                                                ProcessDefinition definition) {

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(procId);
        processInstance.setCaseRef(caseRef);
        processInstance.setDefinition(definition);
        processInstance.setRootActivity(createActivityInstanceFromDefinition(null, definition.getActivityDefinition()));
        return processInstance;
    }

    private ActivityInstance createActivityInstanceFromDefinition(ActivityInstance parentActivity,
                                                                  ActivityDefinition activityDefinition) {
        ActivityInstance currentInstance = new ActivityInstance();
        currentInstance.setId(activityDefinition.getId());
        currentInstance.setDefinition(activityDefinition);
        currentInstance.setState(ActivityState.NOT_STARTED);
        currentInstance.setActivated(null);
        currentInstance.setTerminated(null);

        if (CollectionUtils.isEmpty(activityDefinition.getActivities())) {
            currentInstance.setActivities(Collections.emptyList());
        } else {
            List<ActivityInstance> childActivities = new ArrayList<>(activityDefinition.getActivities().size());
            for (ActivityDefinition childDefinition : activityDefinition.getActivities()) {
                ActivityInstance childActivity = createActivityInstanceFromDefinition(currentInstance, childDefinition);
                childActivities.add(childActivity);
            }
            currentInstance.setActivities(childActivities);
        }

        currentInstance.setVariables(null);
        currentInstance.setParentInstance(parentActivity);
        return currentInstance;
    }

    @Override
    public ProcessInstance getFullState(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);
        String stateId = (String) nodeService.getProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID);
        if (StringUtils.isBlank(stateId)) {
            return null;
        }

        ProcessInstance transactionProcessInstance = getProcessStateFromTransactionByStateId(caseRef);
        if (transactionProcessInstance != null) {
            return transactionProcessInstance;
        }

        GetProcStateResp processState = getProcessStateFromMicroservice(stateId);
        byte[] stateData = processState.getStateData();

        ProcessInstance instance = Json.getMapper().read(stateData, ProcessInstance.class);
        if (instance == null) {
            throw new RuntimeException("Can not parse state from microservice for caseRef=" + caseRef);
        }

        ProcessDefinition definition = getFullDefinitionForExisting(stateId);
        setUnSerializableObjectsInProcessInstance(instance, definition);

        putInstanceToTransactionScopeByStateId(caseRef, instance);

        return instance;
    }

    private GetProcStateResp getProcessStateFromMicroservice(String stateId) {
        GetProcState getProcStateCommand = new GetProcState();
        getProcStateCommand.setProcType(CMMN_PROCESS_TYPE);
        getProcStateCommand.setProcStateId(stateId);

        CommandResult commandResult = commandsService.executeRemoteSync(getProcStateCommand, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while receiving of process state. For detailed information see logs");
        }
        return commandResult.getCommandAs(GetProcStateResp.class);
    }

    private void setUnSerializableObjectsInProcessInstance(ProcessInstance processInstance,
                                                           ProcessDefinition processDefinition) {

        processInstance.setDefinition(processDefinition);

        setUnSerializableObjectsInActivityInstance(null, processInstance.getRootActivity(), processDefinition);
    }

    private void setUnSerializableObjectsInActivityInstance(ActivityInstance parentInstance,
                                                            ActivityInstance activityInstance,
                                                            ProcessDefinition processDefinition) {

        activityInstance.setParentInstance(parentInstance);

        ActivityDefinition activityDefinition = findDefinitionById(processDefinition, activityInstance.getId());
        if (activityDefinition == null) {
            throw new RuntimeException("Can not find definition by id=" + activityInstance.getId());
        }
        activityInstance.setDefinition(activityDefinition);

        if (activityInstance.getActivities() != null) {
            for (ActivityInstance childInstance : activityInstance.getActivities()) {
                setUnSerializableObjectsInActivityInstance(activityInstance, childInstance, processDefinition);
            }
        }
    }

    private ActivityDefinition findDefinitionById(ProcessDefinition processDefinition, String id) {
        ActivityDefinition rootDefinition = processDefinition.getActivityDefinition();
        return findDefinitionById(rootDefinition, id);
    }

    private ActivityDefinition findDefinitionById(ActivityDefinition currentDefinition, String id) {
        if (StringUtils.equals(currentDefinition.getId(), id)) {
            return currentDefinition;
        }

        List<ActivityDefinition> childDefinitions = currentDefinition.getActivities();
        if (CollectionUtils.isEmpty(childDefinitions)) {
            return null;
        }

        for (ActivityDefinition childDefinition : childDefinitions) {
            ActivityDefinition definitionById = findDefinitionById(childDefinition, id);
            if (definitionById != null) {
                return definitionById;
            }
        }
        return null;
    }

    private ProcessInstance getProcessStateFromTransactionByStateId(RecordRef caseRef) {
        String key = getProcessStateTransactionKey(caseRef);
        return TransactionSupportUtil.getResource(key);
    }

    private void putInstanceToTransactionScopeByStateId(RecordRef caseRef, ProcessInstance instance) {
        String key = getProcessStateTransactionKey(caseRef);
        ProcessInstance current = TransactionSupportUtil.getResource(key);
        if (current != null) {
            if (!Objects.equals(instance, current)) {
                throw new RuntimeException("For case='" + caseRef + "' already saved another process instance.\n" +
                        "Existing instance: " + current + ".\n" +
                        "New instance: " + instance + "." +
                        "Perhaps out of sync!");
            }
            return;
        }

        TransactionSupportUtil.bindResource(caseRef, instance);
    }

    private String getProcessStateTransactionKey(RecordRef caseRef) {
        return this.getClass().getName()
                + "."
                + EPROC_CASE_STATE_BY_ID_KEY_PREFIX
                + "."
                + caseRef;
    }

    @Override
    public void saveState(ProcessInstance processInstance) {
        TransactionUtils.processAfterBehaviours(
                EPROC_SAVE_STATE_TRANSACTION_KEY,
                processInstance.getCaseRef(),
                (caseRef) -> saveStateImpl(processInstance));
    }

    private void saveStateImpl(ProcessInstance processInstance) {
        RecordRef caseRef = processInstance.getCaseRef();
        MandatoryParam.check("caseRef", caseRef);

        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);
        String prevStateId = (String) nodeService.getProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID);

        UpdateProcStateResp result = updateStateInMicroservice(prevStateId, processInstance);
        if (result == null || StringUtils.isBlank(result.getProcStateId())) {
            throw new RuntimeException("Error while state saving");
        }

        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID, result.getProcStateId());
    }

    private UpdateProcStateResp updateStateInMicroservice(String prevStateId, ProcessInstance processInstance) {
        UpdateProcState updateProcState = new UpdateProcState();
        updateProcState.setPrevProcStateId(prevStateId);
        byte[] stateData = Json.getMapper().toBytes(processInstance);
        if (stateData == null) {
            throw new IllegalArgumentException("Can not parse processInstance to bytes. " +
                    "For detailed information see logs");
        }
        updateProcState.setStateData(stateData);

        CommandResult commandResult = commandsService.executeRemoteSync(updateProcState, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while state updating. For detailed information see logs");
        }
        return commandResult.getCommandAs(UpdateProcStateResp.class);
    }

    @Override
    public ActivityInstance getStateInstance(ActivityRef activityRef) {
        ProcessInstance instance = getFullState(activityRef.getProcessId());
        if (activityRef.isRoot()) {
            return instance.getRootActivity();
        }

        return getStateInstance(instance.getRootActivity(), activityRef.getId());
    }

    private ActivityInstance getStateInstance(ActivityInstance instance, String id) {
        if (StringUtils.equals(instance.getId(), id)) {
            return instance;
        }

        if (CollectionUtils.isEmpty(instance.getActivities())) {
            return null;
        }

        for (ActivityInstance childInstance : instance.getActivities()) {
            ActivityInstance stateInstance = getStateInstance(childInstance, id);
            if (stateInstance != null) {
                return stateInstance;
            }
        }
        return null;
    }

    @Override
    public SentryDefinition getSentryDefinition(EventRef eventRef) {
        ProcessInstance processInstance = getFullState(eventRef.getProcessId());
        ProcessDefinition processDefinition = processInstance.getDefinition();

        return findSentryRecursiveById(processDefinition.getActivityDefinition(), eventRef.getId());
    }

    private SentryDefinition findSentryRecursiveById(ActivityDefinition definition, String sentryId) {
        Optional<SentryDefinition> result = getAllSentries(definition).stream()
                .filter(sentry -> StringUtils.equals(sentry.getId(), sentryId))
                .findFirst();

        if (result.isPresent()) {
            return result.get();
        }

        if (CollectionUtils.isNotEmpty(definition.getActivities())) {
            for (ActivityDefinition childActivityDefinition : definition.getActivities()) {
                SentryDefinition sentry = findSentryRecursiveById(childActivityDefinition, sentryId);
                if (sentry != null) {
                    return sentry;
                }
            }
        }

        return null;
    }

    @Override
    public List<SentryDefinition> findSentriesBySourceRefAndEventType(RecordRef caseRef, String sourceRef, String eventType) {
        ProcessInstance fullState = getFullState(caseRef);
        ProcessDefinition definition = fullState.getDefinition();
        return findSentriesBySourceRefAndEventType(definition, sourceRef, eventType);
    }

    private List<SentryDefinition> findSentriesBySourceRefAndEventType(ProcessDefinition definition,
                                                                       String sourceRef, String eventType) {
        ActivityDefinition rootActivityDefinition = definition.getActivityDefinition();
        return findSentriesRecursiveBySourceRefAndEventType(rootActivityDefinition, sourceRef, eventType);
    }

    private List<SentryDefinition> findSentriesRecursiveBySourceRefAndEventType(ActivityDefinition definition,
                                                                                String sourceRef, String eventType) {
        List<SentryDefinition> result = getAllSentries(definition).stream()
                .filter(sentry -> StringUtils.equals(sentry.getSourceRef().getRef(), sourceRef))
                .filter(sentry -> StringUtils.equals(sentry.getEvent(), eventType))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(definition.getActivities())) {
            for (ActivityDefinition childActivityDefinition : definition.getActivities()) {
                result.addAll(findSentriesRecursiveBySourceRefAndEventType(childActivityDefinition, sourceRef, eventType));
            }
        }

        return result;
    }

    private List<SentryDefinition> getAllSentries(ActivityDefinition definition) {
        List<SentryDefinition> result = new ArrayList<>();
        for (ActivityTransitionDefinition transitionDef : definition.getTransitions()) {
            TriggerDefinition triggerDef = transitionDef.getTrigger();
            SentryTriggerDefinition sentryTriggerDef = triggerDef.getData().getAs(SentryTriggerDefinition.class);
            if (sentryTriggerDef == null) {
                continue;
            }
            result.addAll(sentryTriggerDef.getSentries());
        }
        return result;
    }

    @Data
    private static class EcosAlfTypesKey {
        private final RecordRef ecosType;
        private final List<String> alfTypes;
    }
}
