package ru.citeck.ecos.icase.activity.service.eproc;

import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.commands.dto.CommandResult;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.commons.utils.MandatoryParam;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.eproc.commands.dto.request.*;
import ru.citeck.ecos.icase.activity.service.eproc.commands.dto.response.*;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnSchemaParser;
import ru.citeck.ecos.icase.activity.service.eproc.importer.pojo.OptimizedProcessDefinition;
import ru.citeck.ecos.icase.activity.service.eproc.importer.pojo.SentrySearchKey;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.node.EcosTypeService;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.utils.TransactionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("eprocActivityService")
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
    private BehaviourFilter behaviourFilter;

    private LoadingCache<EcosAlfTypesKey, String> typesToRevisionIdCache;
    private LoadingCache<String, OptimizedProcessDefinition> revisionIdToProcessDefinitionCache;

    @Autowired
    public EProcActivityServiceImpl(CmmnSchemaParser cmmnSchemaParser,
                                    CommandsService commandsService,
                                    EcosTypeService ecosTypeService,
                                    DictionaryService dictionaryService,
                                    NodeService nodeService,
                                    @Qualifier("policyBehaviourFilter") BehaviourFilter behaviourFilter) {

        this.commandsService = commandsService;
        this.ecosTypeService = ecosTypeService;
        this.cmmnSchemaParser = cmmnSchemaParser;
        this.dictionaryService = dictionaryService;
        this.nodeService = nodeService;
        this.behaviourFilter = behaviourFilter;

        this.typesToRevisionIdCache = CacheBuilder.newBuilder()
                .maximumSize(250)
                .build(CacheLoader.from(this::findProcDefRevIdFromMicroservice));

        this.revisionIdToProcessDefinitionCache = CacheBuilder.newBuilder()
                .maximumSize(150)
                .build(CacheLoader.from(this::getProcessDefByRevIdFromMicroservice));
    }

    @Override
    public Pair<String, OptimizedProcessDefinition> getOptimizedDefinitionWithRevisionId(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);
        String procRevId = getRevisionIdForNode(caseNodeRef);

        OptimizedProcessDefinition result = revisionIdToProcessDefinitionCache.getUnchecked(procRevId);
        if (result == null) {
            return null;
        }
        return new Pair<>(procRevId, result);
    }

    @Override
    public ProcessDefinition getFullDefinition(RecordRef caseRef) {
        return getFullDefinitionImpl(caseRef).getProcessDefinition();
    }

    private OptimizedProcessDefinition getFullDefinitionImpl(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        String defRevId = (String) nodeService.getProperty(caseNodeRef, EcosProcessModel.PROP_DEFINITION_REVISION_ID);
        if (StringUtils.isNotBlank(defRevId)) {
            return getFullDefinitionByRevisionId(defRevId);
        }

        String stateId = (String) nodeService.getProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID);
        if (StringUtils.isNotBlank(stateId)) {
            return getFullDefinitionForExistingByStateId(stateId);
        }

        return getFullDefinitionForNewCase(caseNodeRef);
    }

    private OptimizedProcessDefinition getFullDefinitionForExistingByStateId(String stateId) {
        GetProcStateResp processState = getProcessStateFromMicroservice(stateId);
        if (processState == null) {
            throw new IllegalArgumentException("Can not find state for stateId=" + stateId);
        }

        String procDefRevId = processState.getProcDefRevId();
        return getFullDefinitionByRevisionId(procDefRevId);
    }

    private OptimizedProcessDefinition getFullDefinitionForNewCase(NodeRef caseNodeRef) {
        String processRevisionId = getRevisionIdForNode(caseNodeRef);
        return getFullDefinitionByRevisionId(processRevisionId);
    }

    private OptimizedProcessDefinition getFullDefinitionByRevisionId(String processRevisionId) {
        OptimizedProcessDefinition result = revisionIdToProcessDefinitionCache.getUnchecked(processRevisionId);
        if (result == null || result.getProcessDefinition() == null) {
            throw new IllegalArgumentException("Can not find processDef by procDefRevId=" + processRevisionId);
        }
        return result;
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

        List<QName> result = new ArrayList<>();

        ClassDefinition typeDef = dictionaryService.getClass(type);
        while (typeDef != null) {
            result.add(typeDef.getName());
            typeDef = typeDef.getParentClassDefinition();
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

        CommandResult commandResult = commandsService.executeSync(findProcDefCommand, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while find of process definition. " +
                    "For detailed information see logs");
        }

        FindProcDefResp response = commandResult.getResultAs(FindProcDefResp.class);
        if (response == null) {
            return null;
        }
        return response.getProcDefRevId();
    }

    private OptimizedProcessDefinition getProcessDefByRevIdFromMicroservice(String definitionRevisionId) {
        GetProcDefRevResp response = getProcessDefByRevIdFromMicroserviceImpl(definitionRevisionId);
        if (response == null) {
            return null;
        }

        return cmmnSchemaParser.parse(response.getData());
    }

    private GetProcDefRevResp getProcessDefByRevIdFromMicroserviceImpl(String definitionRevisionId) {
        GetProcDefRev getProcDefRevCommand = new GetProcDefRev();
        getProcDefRevCommand.setProcType(CMMN_PROCESS_TYPE);
        getProcDefRevCommand.setProcDefRevId(definitionRevisionId);

        CommandResult commandResult = commandsService.executeSync(getProcDefRevCommand, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while receiving of process definition revision. " +
                    "For detailed information see logs");
        }

        return commandResult.getResultAs(GetProcDefRevResp.class);
    }

    @Override
    public ProcessInstance createDefaultState(RecordRef caseRef) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        EcosAlfTypesKey ecosAlfTypesKey = composeEcosAlfTypesKey(caseNodeRef);
        String definitionRevisionId = typesToRevisionIdCache.getUnchecked(ecosAlfTypesKey);
        CreateProcResp createProcResp = createProcessInstanceInMicroservice(definitionRevisionId, caseRef);
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_PROCESS_ID, createProcResp.getProcId());
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID, createProcResp.getProcStateId());
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_DEFINITION_REVISION_ID, definitionRevisionId);

        OptimizedProcessDefinition optimizedProcessDefinition = getFullDefinitionForNewCase(caseNodeRef);
        ProcessInstance processInstance = createProcessInstanceFromDefinition(createProcResp.getProcId(),
                caseRef, optimizedProcessDefinition.getProcessDefinition());

        putInstanceToTransactionScopeByStateId(caseRef, processInstance);

        return processInstance;
    }

    @Override
    public ProcessInstance createDefaultState(RecordRef caseRef, String revisionId,
                                              OptimizedProcessDefinition optimizedProcessDefinition) {
        NodeRef caseNodeRef = RecordsUtils.toNodeRef(caseRef);

        CreateProcResp createProcResp = createProcessInstanceInMicroservice(revisionId, caseRef);
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_PROCESS_ID, createProcResp.getProcId());
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID, createProcResp.getProcStateId());
        nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_DEFINITION_REVISION_ID, revisionId);

        ProcessInstance processInstance = createProcessInstanceFromDefinition(createProcResp.getProcId(),
                caseRef, optimizedProcessDefinition.getProcessDefinition());

        putInstanceToTransactionScopeByStateId(caseRef, processInstance);

        return processInstance;
    }

    private CreateProcResp createProcessInstanceInMicroservice(String definitionRevisionId, RecordRef caseRef) {
        CreateProc createProc = new CreateProc();
        createProc.setProcDefRevId(definitionRevisionId);
        createProc.setRecordRef(caseRef);

        CommandResult commandResult = commandsService.executeSync(createProc, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while creation of process state. For detailed information see logs");
        }
        return commandResult.getResultAs(CreateProcResp.class);
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

        OptimizedProcessDefinition optimizedProcessDefinition = getFullDefinitionImpl(caseRef);
        setUnSerializableObjectsInProcessInstance(instance, optimizedProcessDefinition);

        putInstanceToTransactionScopeByStateId(caseRef, instance);

        return instance;
    }

    private GetProcStateResp getProcessStateFromMicroservice(String stateId) {
        GetProcState getProcStateCommand = new GetProcState();
        getProcStateCommand.setProcType(CMMN_PROCESS_TYPE);
        getProcStateCommand.setProcStateId(stateId);

        CommandResult commandResult = commandsService.executeSync(getProcStateCommand, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while receiving of process state. For detailed information see logs");
        }
        return commandResult.getResultAs(GetProcStateResp.class);
    }

    private void setUnSerializableObjectsInProcessInstance(ProcessInstance processInstance,
                                                           OptimizedProcessDefinition optimizedProcessDefinition) {

        processInstance.setDefinition(optimizedProcessDefinition.getProcessDefinition());

        setUnSerializableObjectsInActivityInstance(null, processInstance.getRootActivity(), optimizedProcessDefinition);
    }

    private void setUnSerializableObjectsInActivityInstance(ActivityInstance parentInstance,
                                                            ActivityInstance activityInstance,
                                                            OptimizedProcessDefinition optimizedProcessDefinition) {

        activityInstance.setParentInstance(parentInstance);

        ActivityDefinition activityDefinition = findDefinitionById(optimizedProcessDefinition, activityInstance.getId());
        if (activityDefinition == null) {
            throw new RuntimeException("Can not find definition by id=" + activityInstance.getId());
        }
        activityInstance.setDefinition(activityDefinition);

        if (activityInstance.getActivities() != null) {
            for (ActivityInstance childInstance : activityInstance.getActivities()) {
                setUnSerializableObjectsInActivityInstance(activityInstance, childInstance, optimizedProcessDefinition);
            }
        }
    }

    private ActivityDefinition findDefinitionById(OptimizedProcessDefinition optimizedProcessDefinition, String id) {
        return optimizedProcessDefinition.getIdToActivityCache().get(id);
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

        TransactionSupportUtil.bindResource(key, instance);
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

        try {
            behaviourFilter.disableBehaviour(caseNodeRef);
            nodeService.setProperty(caseNodeRef, EcosProcessModel.PROP_STATE_ID, result.getProcStateId());
        } finally {
            behaviourFilter.enableBehaviour(caseNodeRef);
        }
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

        CommandResult commandResult = commandsService.executeSync(updateProcState, EPROC_TARGET_APP_NAME);
        if (CollectionUtils.isNotEmpty(commandResult.getErrors())) {
            throw new RuntimeException("Exception while state updating. For detailed information see logs");
        }
        return commandResult.getResultAs(UpdateProcStateResp.class);
    }

    @Override
    public ActivityInstance getStateInstance(ActivityRef activityRef) {
        ProcessInstance instance = getFullState(activityRef.getProcessId());
        if (activityRef.isRoot()) {
            return instance.getRootActivity();
        }

        return getStateInstanceRecursively(instance.getRootActivity(), activityRef.getId());
    }

    private ActivityInstance getStateInstanceRecursively(ActivityInstance instance, String id) {
        if (StringUtils.equals(instance.getId(), id)) {
            return instance;
        }

        if (CollectionUtils.isEmpty(instance.getActivities())) {
            return null;
        }

        for (ActivityInstance childInstance : instance.getActivities()) {
            ActivityInstance stateInstance = getStateInstanceRecursively(childInstance, id);
            if (stateInstance != null) {
                return stateInstance;
            }
        }
        return null;
    }

    @Override
    public ActivityDefinition getActivityDefinition(ActivityRef activityRef) {
        OptimizedProcessDefinition optimizedProcessDefinition = getFullDefinitionImpl(activityRef.getProcessId());
        return optimizedProcessDefinition.getIdToActivityCache().get(activityRef.getId());
    }

    @Override
    public SentryDefinition getSentryDefinition(EventRef eventRef) {
        OptimizedProcessDefinition optimizedProcessDefinition = getFullDefinitionImpl(eventRef.getProcessId());
        return optimizedProcessDefinition.getIdToSentryCache().get(eventRef.getId());
    }

    @Override
    public List<SentryDefinition> findSentriesBySourceRefAndEventType(RecordRef caseRef, String sourceRef, String eventType) {
        OptimizedProcessDefinition optimizedProcessDefinition = getFullDefinitionImpl(caseRef);
        SourceRef sourceRefObj = new SourceRef();
        sourceRefObj.setRef(sourceRef);
        SentrySearchKey sentrySearchKey = new SentrySearchKey(sourceRefObj, eventType);
        return optimizedProcessDefinition.getSentrySearchCache().get(sentrySearchKey);
    }

    @Data
    private static class EcosAlfTypesKey {
        private final RecordRef ecosType;
        private final List<String> alfTypes;
    }
}
