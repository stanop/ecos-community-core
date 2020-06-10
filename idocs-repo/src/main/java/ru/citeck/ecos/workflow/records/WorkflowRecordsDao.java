package ru.citeck.ecos.workflow.records;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.value.EmptyValue;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.MutableRecordsDao;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDao;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsMetaDao;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsQueryWithMetaDao;
import ru.citeck.ecos.workflow.EcosWorkflowService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowRecordsDao extends LocalRecordsDao
                                implements LocalRecordsQueryWithMetaDao<WorkflowRecordsDao.WorkflowRecord>,
                                           LocalRecordsMetaDao<MetaValue>,
                                           MutableRecordsDao {

    private static final String ID = "workflow";
    private static final int MIN_RECORDS_SIZE = 0;
    private static final int MAX_RECORDS_SIZE = 10000;
    private final WorkflowRecord EMPTY_RECORD = new WorkflowRecord();

    private final EcosWorkflowService ecosWorkflowService;
    private final NodeService nodeService;

    @Autowired
    public WorkflowRecordsDao(EcosWorkflowService ecosWorkflowService,
                              NodeService nodeService) {
        setId(ID);
        this.ecosWorkflowService = ecosWorkflowService;
        this.nodeService = nodeService;
    }

    @Override
    public List<MetaValue> getLocalRecordsMeta(List<RecordRef> list, MetaField metaField) {

        if (list.size() == 1 && list.get(0).getId().isEmpty()) {
            return Collections.singletonList(EMPTY_RECORD);
        }

        return list.stream()
            .map(ref -> {
                if (ref.getId().isEmpty()) {
                    return EMPTY_RECORD;
                }
                WorkflowInstance instance = ecosWorkflowService.getInstanceById(ref.getId());
                if (instance != null) {
                    return new WorkflowRecord(ecosWorkflowService.getInstanceById(ref.getId()));
                } else {
                    return EmptyValue.INSTANCE;
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public RecordsQueryResult<WorkflowRecord> queryLocalRecords(RecordsQuery recordsQuery, MetaField metaField) {

        RecordsQueryResult<WorkflowRecord> result = new RecordsQueryResult<>();

        WorkflowRecordsDao.WorkflowQuery queryData = recordsQuery.getQuery(WorkflowRecordsDao.WorkflowQuery.class);

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
    public RecordsMutResult mutateImpl(RecordsMutation mutation) {

        RecordsMutResult result = new RecordsMutResult();

        List<RecordMeta> handledMeta = mutation.getRecords().stream()
            .map(this::cancelWorkflowIfRequired)
            .collect(Collectors.toList());

        result.setRecords(handledMeta);
        return result;
    }

    private RecordMeta cancelWorkflowIfRequired(RecordMeta meta) {
        if (meta.hasAttribute("cancel")) {
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
                case "previewInfo":
                    WorkflowContentInfo contentInfo = new WorkflowContentInfo();
                    String url = "alfresco/api/workflow-instances/" + instance.getId() + "/diagram";
                    contentInfo.setUrl(url);
                    contentInfo.setExt("png");
                    contentInfo.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);
                    return contentInfo;
                case "document":
                    NodeRef wfPackageNodeRef = instance.getWorkflowPackage();
                    return nodeService.getProperty(wfPackageNodeRef, CiteckWorkflowModel.PROP_ATTACHED_DOCUMENT);
            }
            return null;
        }

        @Override
        public String getDisplayName() {
            String dispName = instance.getDescription();
            if (StringUtils.isBlank(dispName) && instance.getDefinition() != null) {
                dispName = instance.getDefinition().getTitle();
            }
            return dispName;
        }

        @Override
        public RecordRef getRecordType() {
            return RecordRef.create("emodel", "type", "workflow");
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
    }

    @Data
    public static class WorkflowQuery {
        private Boolean active;
    }
}
