package ru.citeck.ecos.records.workflow;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.model.WorkflowMirrorModel;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.query.QueryConsistency;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.utils.AuthorityUtils;
import ru.citeck.ecos.workflow.tasks.EcosTaskService;
import ru.citeck.ecos.workflow.tasks.TaskInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.citeck.ecos.records.workflow.WorkflowTaskRecordsConstants.*;

@Log4j
@Component
public class WorkflowTaskRecordsUtils {

    private final AuthorityUtils authorityUtils;
    private final NamespaceService namespaceService;
    private final RecordsService recordsService;
    private final EcosTaskService ecosTaskService;
    private final CaseStatusService caseStatusService;

    @Autowired
    public WorkflowTaskRecordsUtils(AuthorityUtils authorityUtils, NamespaceService namespaceService,
                                    RecordsService recordsService, EcosTaskService ecosTaskService,
                                    CaseStatusService caseStatusService) {
        this.authorityUtils = authorityUtils;
        this.namespaceService = namespaceService;
        this.recordsService = recordsService;
        this.ecosTaskService = ecosTaskService;
        this.caseStatusService = caseStatusService;
    }

    ComposedPredicate buildPredicateQuery(WorkflowTaskRecords.TasksQuery tasksQuery) {

        AndPredicate predicate = new AndPredicate();
        predicate.addPredicate(ValuePredicate.equal("TYPE", WorkflowModel.TYPE_TASK.toString()));

        List<String> actors = tasksQuery.getActors();
        appendActorsPredicate(actors, predicate);

        Boolean active = tasksQuery.active;
        appendActivePredicate(active, predicate);

        String caseStatus = tasksQuery.getDocStatus();
        appendCaseStatusPredicate(caseStatus, predicate);

        String docType = tasksQuery.docType;
        appendDocTypePredicate(docType, predicate);

        String documentParam = tasksQuery.document;
        appendDocumentParamPredicate(documentParam, predicate);

        if (predicate.getPredicates().isEmpty()) {
            return null;
        }

        return predicate;
    }

    private void appendActivePredicate(Boolean active, AndPredicate predicate) {
        if (active != null) {
            Predicate completionEmpty = new EmptyPredicate("bpm:completionDate");
            if (active) {
                predicate.addPredicate(completionEmpty);
            } else {
                predicate.addPredicate(new NotPredicate(completionEmpty));
            }
        }
    }

    private void appendActorsPredicate(List<String> actors, AndPredicate predicate) {
        if (actors != null) {
            predicate.addPredicate(getActorsPredicate(actors));
        }
    }

    private void appendCaseStatusPredicate(String caseStatus, AndPredicate predicate) {
        if (StringUtils.isNotBlank(caseStatus)) {
            if (!NodeRef.isNodeRef(caseStatus)) {
                NodeRef ref = caseStatusService.getStatusByName(caseStatus.toLowerCase());
                if (ref != null) {
                    caseStatus = ref.toString();
                }
            }
            Predicate caseStatusPredicate = ValuePredicate.equal(
                    WorkflowMirrorModel.PROP_CASE_STATUS.toPrefixString(namespaceService), caseStatus);
            predicate.addPredicate(caseStatusPredicate);
        }
    }

    private void appendDocTypePredicate(String docType, AndPredicate predicate) {
        if (StringUtils.isNotBlank(docType)) {
            if (docType.contains(":") || docType.contains("{")) {
                Predicate typePredicate;
                QName docTypeQName = QName.resolveToQName(namespaceService, docType);
                String docTypeAtt = WorkflowMirrorModel.PROP_DOCUMENT_TYPE.toPrefixString(namespaceService);
                if (docTypeQName != null) {
                    typePredicate = ValuePredicate.equal(docTypeAtt, docTypeQName.toString());
                } else {
                    log.warn("Document type qname " + docType + " is not found");
                    typePredicate = ValuePredicate.equal(docTypeAtt, docType);
                }
                predicate.addPredicate(typePredicate);
            }
        }
    }

    private void appendDocumentParamPredicate(String documentParam, AndPredicate predicate) {
        if (StringUtils.isNotBlank(documentParam)) {
            if (!NodeRef.isNodeRef(documentParam)) {
                return;
            }

            String docAttr = WorkflowMirrorModel.PROP_DOCUMENT.toPrefixString(namespaceService);
            Predicate documentPredicate = ValuePredicate.equal(docAttr, documentParam);

            predicate.addPredicate(documentPredicate);
        }
    }

    private OrPredicate getActorsPredicate(List<String> actors) {
        Set<String> actorRefs = actors.stream().flatMap(actor -> {
            if (CURRENT_USER.equals(actor)) {
                actor = AuthenticationUtil.getRunAsUser();
            } else if (NodeRef.isNodeRef(actor)) {
                actor = authorityUtils.getAuthorityName(new NodeRef(actor));
            }
            return Stream.concat(authorityUtils.getContainingAuthoritiesRefs(actor).stream(),
                    Stream.of(authorityUtils.getNodeRef(actor)))
                    .map(NodeRef::toString);
        }).collect(Collectors.toSet());

        OrPredicate orPred = new OrPredicate();
        actorRefs.forEach(a -> {
            ValuePredicate valuePredicate = new ValuePredicate();
            valuePredicate.setType(ValuePredicate.Type.CONTAINS);
            valuePredicate.setAttribute("wfm:actors");
            valuePredicate.setValue(a);
            orPred.addPredicate(valuePredicate);
        });

        return orPred;
    }

    RecordsQueryResult<WorkflowTaskRecords.TaskIdQuery> queryTasks(ComposedPredicate predicate, RecordsQuery query) {
        RecordsQuery taskRecordsQuery = new RecordsQuery();
        taskRecordsQuery.setLanguage(PredicateService.LANGUAGE_PREDICATE);
        taskRecordsQuery.setQuery(predicate);
        taskRecordsQuery.setSortBy(query.getSortBy());

        WorkflowTaskRecords.TasksQuery tasksQuery = query.getQuery(WorkflowTaskRecords.TasksQuery.class);
        boolean filterByActiveTaskRequired = Boolean.TRUE.equals(tasksQuery.active);

        if (query.getMaxItems() > -1 && filterByActiveTaskRequired) {
            taskRecordsQuery.setMaxItems(query.getMaxItems() * 5); //we need to increase the selection for filtering
        } else {
            taskRecordsQuery.setMaxItems(query.getMaxItems());
        }

        taskRecordsQuery.setSkipCount(query.getSkipCount());
        taskRecordsQuery.setDebug(query.isDebug());
        taskRecordsQuery.setConsistency(QueryConsistency.EVENTUAL);

        RecordsQueryResult<WorkflowTaskRecords.TaskIdQuery> result = recordsService.queryRecords(taskRecordsQuery,
                WorkflowTaskRecords.TaskIdQuery.class);

        if (filterByActiveTaskRequired) {
            filterActiveTask(result, query);
        }

        return result;
    }

    private void filterActiveTask(RecordsQueryResult<WorkflowTaskRecords.TaskIdQuery> taskQueryResult,
                                  RecordsQuery query) {
        log.debug("Start filterActiveTask...");

        final int maxItems = query.getMaxItems();
        long taskQueryResultTotalCount = taskQueryResult.getTotalCount();

        FilterByActiveTask resultOfFiltering = new FilterByActiveTask(taskQueryResult, maxItems);

        List<WorkflowTaskRecords.TaskIdQuery> filteredRecords = resultOfFiltering.getFilteredData();
        AtomicInteger recordsCount = resultOfFiltering.getRecordsCount();
        AtomicInteger filteredCount = resultOfFiltering.getFilteredCount();

        long totalCount = taskQueryResultTotalCount - filteredCount.get();

        taskQueryResult.setRecords(filteredRecords);
        taskQueryResult.setTotalCount(totalCount);

        log.debug(String.format("finish filtering:" +
                        "\nresult:%s" +
                        "\nmaxItems:%s" +
                        "\ntaskQueryResultTotalCount:%s" +
                        "\nrecordsCount:%s" +
                        "\nfilteredCount:%s", filteredRecords, maxItems, taskQueryResultTotalCount, recordsCount,
                filteredCount));
    }

    private boolean taskIsActive(String taskId) {
        Optional<TaskInfo> taskInfo = ecosTaskService.getTaskInfo(taskId);
        boolean active = taskInfo.filter(info -> info.getAttribute("bpm_completionDate") == null).isPresent();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Task <%s>, is active: %s", taskId, active));
        }

        return active;
    }

    boolean isReassignable(Map<String, Object> attributes, boolean hasOwner, boolean hasClaimOwner) {
        boolean bpmIsReassignable = Boolean.TRUE.equals(attributes.get("bpm_reassignable"));
        boolean isReassignableAllowed = bpmIsReassignable && (hasOwner || hasClaimOwner);
        boolean isReassignableDisabled = Boolean.FALSE.equals(attributes.get("cwf_isTaskReassignable"));
        return isReassignableAllowed && !isReassignableDisabled;
    }

    boolean isClaimable(Map<String, Object> attributes, boolean hasOwner, boolean hasClaimOwner,
                        boolean hasPooledActors) {
        boolean isClaimableAllowed = hasPooledActors && (!hasOwner && !hasClaimOwner);
        boolean isClaimableDisabled = Boolean.FALSE.equals(attributes.get("cwf_isTaskClaimable"));
        return isClaimableAllowed && !isClaimableDisabled;
    }

    boolean isAssignable(Map<String, Object> attributes, boolean hasOwner, boolean hasClaimOwner,
                         boolean hasPooledActors) {
        return isClaimable(attributes, hasOwner, hasClaimOwner, hasPooledActors);
    }

    boolean isReleasable(Map<String, Object> attributes, boolean hasOwner, boolean hasClaimOwner,
                         boolean hasPooledActors) {
        boolean isReleasableAllowed = hasPooledActors && (hasOwner || hasClaimOwner);
        boolean isReleasableDisabled = Boolean.FALSE.equals(attributes.get("cwf_isTaskReleasable"));
        return isReleasableAllowed && !isReleasableDisabled;
    }

    @Getter
    private class FilterByActiveTask {

        private AtomicInteger recordsCount = new AtomicInteger(0);
        private AtomicInteger filteredCount = new AtomicInteger(0);
        private List<WorkflowTaskRecords.TaskIdQuery> filteredData;

        FilterByActiveTask(RecordsQueryResult<WorkflowTaskRecords.TaskIdQuery> data, int maxItems) {
            filteredData = data.getRecords()
                    .stream()
                    .filter(taskIdQuery -> {
                        if (maxItems > -1 && maxItems <= recordsCount.getAndIncrement()) {
                            return false;
                        }

                        if (taskIsActive(taskIdQuery.getTaskId())) {
                            return true;
                        } else {
                            filteredCount.incrementAndGet();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

}
