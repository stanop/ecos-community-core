package ru.citeck.ecos.records.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.WorkflowMirrorModel;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.predicate.model.*;
import ru.citeck.ecos.records2.RecordRef;
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

    @Autowired
    public WorkflowTaskRecordsUtils(AuthorityUtils authorityUtils, NamespaceService namespaceService,
                                    RecordsService recordsService, EcosTaskService ecosTaskService) {
        this.authorityUtils = authorityUtils;
        this.namespaceService = namespaceService;
        this.recordsService = recordsService;
        this.ecosTaskService = ecosTaskService;
    }

    ComposedPredicate buildPredicateQuery(WorkflowTaskRecords.TasksQuery tasksQuery) {

        AndPredicate predicate = new AndPredicate();
        predicate.addPredicate(ValuePredicate.equal("TYPE", WorkflowModel.TYPE_TASK.toString()));

        List<String> actors = tasksQuery.getActors();
        if (actors != null) {
            predicate.addPredicate(getActorsPredicate(actors));
        }

        if (tasksQuery.active != null) {
            Predicate completionEmpty = new EmptyPredicate("bpm:completionDate");
            if (tasksQuery.active) {
                predicate.addPredicate(completionEmpty);
            } else {
                predicate.addPredicate(new NotPredicate(completionEmpty));
            }
        }

        String caseStatus = tasksQuery.getDocStatus();
        if (StringUtils.isNotBlank(caseStatus)) {
            Predicate caseStatusPredicate = ValuePredicate.equal(
                    WorkflowMirrorModel.PROP_CASE_STATUS.toPrefixString(namespaceService), caseStatus);
            predicate.addPredicate(caseStatusPredicate);
        }

        String docType = tasksQuery.docType;
        if (StringUtils.isNotBlank(docType)) {

            Predicate typePredicate = null;

            if (docType.contains(":") || docType.contains("{")) {
                QName docTypeQName = QName.resolveToQName(namespaceService, docType);
                String docTypeAtt = WorkflowMirrorModel.PROP_DOCUMENT_TYPE.toPrefixString(namespaceService);
                if (docTypeQName != null) {
                    typePredicate = ValuePredicate.equal(docTypeAtt, docTypeQName.toString());
                } else {
                    log.warn("Document type qname " + docType + " is not found");
                    typePredicate = ValuePredicate.equal(docTypeAtt, docType);
                }
            }

            if (typePredicate == null) {
                return null;
            }

            predicate.addPredicate(typePredicate);
        }

        String documentParam = tasksQuery.document;
        if (StringUtils.isNotBlank(documentParam)) {
            if (!NodeRef.isNodeRef(documentParam)) {
                return null;
            }

            String docAttr = WorkflowMirrorModel.PROP_DOCUMENT.toPrefixString(namespaceService);
            Predicate documentPredicate = ValuePredicate.equal(docAttr, documentParam);

            predicate.addPredicate(documentPredicate);
        }

        if (predicate.getPredicates().isEmpty()) {
            return null;
        }

        return predicate;
    }

    private OrPredicate getActorsPredicate(List<String> actors) {
        Set<String> actorRefs = actors.stream().flatMap(actor -> {
            if (CURRENT_USER.equals(actor)) {
                actor = AuthenticationUtil.getRunAsUser();
            } else if (actor.startsWith("workspace://")) {
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
        boolean filterByDocStatusRequired = StringUtils.isNotBlank(tasksQuery.docStatus);
        boolean filterByActiveTaskRequired = Boolean.TRUE.equals(tasksQuery.active);

        if (query.getMaxItems() > -1 && (filterByDocStatusRequired || filterByActiveTaskRequired)) {
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

        if (!filterByDocStatusRequired) {
            return result;
        }

        return filteredByDocStatus(query, result, tasksQuery.docStatus);
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

    private RecordsQueryResult<WorkflowTaskRecords.TaskIdQuery> filteredByDocStatus(RecordsQuery query,
                                                                                    RecordsQueryResult<WorkflowTaskRecords
                                                                                            .TaskIdQuery> taskQueryResult,
                                                                                    @NonNull String docStatus) {
        String statusAttribute;
        if (docStatus.startsWith(SPACES_STORE_PREFIX)) {
            statusAttribute = "icase:caseStatusAssoc?id";
        } else {
            statusAttribute = "icase:caseStatusAssoc.cm:name?str";
        }

        int maxItems = query.getMaxItems();
        AtomicInteger recordsCount = new AtomicInteger(0);
        AtomicInteger filtered = new AtomicInteger(0);

        List<WorkflowTaskRecords.TaskIdQuery> taskRecords = taskQueryResult.getRecords().stream().filter(taskIdQuery -> {

            if (maxItems > -1 && maxItems <= recordsCount.getAndIncrement()) {
                return false;
            }

            Optional<TaskInfo> taskInfo = ecosTaskService.getTaskInfo(taskIdQuery.taskId);
            if (!taskInfo.isPresent()) {
                filtered.incrementAndGet();
                return false;
            }
            RecordRef document = taskInfo.get().getDocument();
            if (document == null) {
                filtered.incrementAndGet();
                return false;
            }

            JsonNode status = recordsService.getAttribute(document, statusAttribute);
            if (status.isTextual() && status.toString().contains(docStatus)) {
                return true;
            } else {
                filtered.incrementAndGet();
                return false;
            }

        }).collect(Collectors.toList());

        taskQueryResult.setRecords(taskRecords);
        taskQueryResult.setTotalCount(taskQueryResult.getTotalCount() - filtered.get());

        return taskQueryResult;
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
