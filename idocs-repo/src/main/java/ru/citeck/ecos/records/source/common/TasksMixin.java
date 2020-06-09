package ru.citeck.ecos.records.source.common;

import lombok.AllArgsConstructor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.source.alf.AlfNodesRecordsDAO;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.source.common.AttributesMixin;
import ru.citeck.ecos.utils.WorkflowUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TasksMixin implements AttributesMixin<Class<RecordRef>, RecordRef> {

    public static final String ATTRIBUTE_NAME = "tasks";

    private final AlfNodesRecordsDAO alfNodesRecordsDAO;
    private final WorkflowUtils workflowUtils;
    private final NodeService nodeService;

    @Autowired
    public TasksMixin(NodeService nodeService,
                      AlfNodesRecordsDAO alfNodesRecordsDAO,
                      WorkflowUtils workflowUtils) {

        this.nodeService = nodeService;
        this.workflowUtils = workflowUtils;
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }

    @PostConstruct
    public void setup() {
        alfNodesRecordsDAO.addAttributesMixin(this);
    }

    @Override
    public List<String> getAttributesList() {
        return Collections.singletonList(ATTRIBUTE_NAME);
    }

    @Override
    public Object getAttribute(String s, RecordRef recordRef, MetaField metaField) {
        String id = recordRef.getId();
        if (!NodeRef.isNodeRef(id)) {
            return null;
        }
        NodeRef nodeRef = new NodeRef(id);
        if (!nodeService.exists(nodeRef)) {
            return null;
        }
        return new TasksValue(nodeRef);
    }

    @Override
    public Class<RecordRef> getMetaToRequest() {
        return RecordRef.class;
    }

    @AllArgsConstructor
    private class TasksValue implements MetaValue {

        private static final String ACTIVE_HASH = "active-hash";
        private final NodeRef nodeRef;

        @Override
        public Object getAttribute(String name, MetaField field) {

            if (ACTIVE_HASH.equals(name)) {
                List<WorkflowTask> tasks = workflowUtils.getDocumentTasks(nodeRef, true);
                return tasks.stream()
                    .map(t -> Objects.hash(t.getId(), t.getProperties()))
                    .collect(Collectors.toList())
                    .hashCode();
            }

            return null;
        }
    }
}
