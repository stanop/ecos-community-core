package ru.citeck.ecos.workflow.records;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.icase.completeness.records.CaseDocumentRecord;
import ru.citeck.ecos.icase.completeness.records.CaseDocumentRecordsDAO;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.predicate.PredicateService;
import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.request.result.RecordsResult;
import ru.citeck.ecos.records2.source.dao.MutableRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.MutableRecordsLocalDAO;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsMetaDAO;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsQueryWithMetaDAO;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.workflow.EcosWorkflowService;
import ru.citeck.ecos.workflow.tasks.TaskInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowRecordsDAO extends LocalRecordsDAO
    implements LocalRecordsQueryWithMetaDAO<WorkflowRecordsDAO.WorkflowRecord>,
    LocalRecordsMetaDAO<WorkflowRecordsDAO.WorkflowRecord>,
    MutableRecordsDAO {

    private static final String ID = "workflow";
    private static final int MIN_RECORDS_SIZE = 0;
    private static final int MAX_RECORDS_SIZE = 10000;
    private final WorkflowRecord EMPTY_RECORD = new WorkflowRecord();

    private final EcosWorkflowService ecosWorkflowService;
    private final NodeService nodeService;

    @Autowired
    public WorkflowRecordsDAO(EcosWorkflowService ecosWorkflowService,
                              NodeService nodeService) {
        setId(ID);
        this.ecosWorkflowService = ecosWorkflowService;
        this.nodeService = nodeService;
    }

    @Override
    public List<WorkflowRecord> getLocalRecordsMeta(List<RecordRef> list, MetaField metaField) {

        if (list.size() == 1 && list.get(0).getId().isEmpty()) {
            return Collections.singletonList(EMPTY_RECORD);
        }

        return list.stream()
            .map(ref -> new WorkflowRecord(ecosWorkflowService.getInstanceById(ref.getId())))
            .collect(Collectors.toList());
    }

    @Override
    public RecordsQueryResult<WorkflowRecord> queryLocalRecords(RecordsQuery recordsQuery, MetaField metaField) {

        RecordsQueryResult<WorkflowRecord> result = new RecordsQueryResult<>();

        WorkflowRecordsDAO.WorkflowQuery queryData = recordsQuery.getQuery(WorkflowRecordsDAO.WorkflowQuery.class);

        WorkflowInstanceQuery query = new WorkflowInstanceQuery();
        if (queryData != null && queryData.active != null) {
            query.setActive(queryData.active);
        }

        int max = recordsQuery.getMaxItems();
        if (max <= MIN_RECORDS_SIZE) {
            max = MAX_RECORDS_SIZE;
        }

        int skipCount = recordsQuery.getSkipCount();

        List<WorkflowInstance> workflowInstances = ecosWorkflowService.getAllInstances(query, max, skipCount);

        List<WorkflowRecord> workflowRecords = workflowInstances.stream()
            .map(WorkflowRecord::new)
            .collect(Collectors.toList());

        result.setRecords(workflowRecords);
        return result;
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutResult result = new RecordsMutResult();

        List<RecordMeta> handledMeta = mutation.getRecords().stream()
            .map(this::handleMeta)
            .collect(Collectors.toList());

        result.setRecords(handledMeta);
        return result;
    }

    private RecordMeta handleMeta(RecordMeta meta) {
        if (meta.hasAttribute("id") && meta.hasAttribute("cancel")) {
            boolean cancel = meta.getAttribute("cancel").asBoolean();
            if (cancel) {
                WorkflowInstance mutatedInstance = ecosWorkflowService.cancelWorkflowInstance(meta.getId().getId());
                meta.setId(mutatedInstance.getId());
            }
        }
        return meta;
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion recordsDeletion) {
        throw new UnsupportedOperationException("Deleting of workflow processes is not supporting!");
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public class WorkflowRecord implements MetaValue {

        private WorkflowInstance instance;

        @Override
        public String getId() {
            return instance.getId();
        }

        @Override
        public Object getAttribute(String name, MetaField field) {
            switch (name) {
                case "_etype":
                    return RecordRef.create("emodel", "type", "workflow");
                case "previewInfo":
                    WorkflowContentInfo contentInfo = new WorkflowContentInfo();
                    String url = "/share/proxy/alfresco/api/workflow-instances/" + instance.getId() + "/diagram";
                    contentInfo.setUrl(url);
                    return contentInfo;
                case "document":
                    NodeRef wfPackageNodeRef = instance.getWorkflowPackage();
                    return nodeService.getProperty(wfPackageNodeRef, CiteckWorkflowModel.PROP_ATTACHED_DOCUMENT);
            }
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowContentInfo {

        private String url;
        private String originalUrl;
        private String originalName;
        private String originalExt;
        private String ext;
        private String mimetype;

        public WorkflowContentInfo(String url, String originalUrl, String originalName,
                                   String originalExt, String ext, String mimetype) {
            this.url = url;
            this.originalUrl = originalUrl;
            this.originalName = originalName;
            this.originalExt = originalExt;
            this.ext = ext;
            this.mimetype = mimetype;
        }
    }

    @Data
    public static class WorkflowQuery {
        private Boolean active;
    }
}
