package ru.citeck.ecos.workflow.listeners;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.workflow.listeners.model.TaskListenerDocumentInfo;

import java.io.Serializable;
import java.util.Map;

@Component
public class TaskDataListenerUtils {

    private static final String DOC_TITLE_TEMPLATE = "%s|%s";

    private final RecordsService recordsService;
    private final NodeService nodeService;

    @Autowired
    public TaskDataListenerUtils(RecordsService recordsService, NodeService nodeService) {
        this.recordsService = recordsService;
        this.nodeService = nodeService;
    }

    public void fillDocumentData(NodeRef documentRef, Map<QName, Serializable> eventProperties) {
        if (documentRef != null && nodeService.exists(documentRef)) {
            TaskListenerDocumentInfo documentMeta = recordsService.getMeta(
                RecordRef.create("", documentRef.toString()), TaskListenerDocumentInfo.class);
            eventProperties.put(HistoryModel.PROP_DOC_TYPE, documentMeta.getDocumentType());
            eventProperties.put(HistoryModel.PROP_DOC_STATUS_NAME, documentMeta.getStatusName());

            //TODO: rewrite to normal format?
            eventProperties.put(HistoryModel.PROP_DOC_STATUS_TITLE, String.format(DOC_TITLE_TEMPLATE,
                documentMeta.getStatusTitleEn(), documentMeta.getStatusTitleRu()));
        }
    }
}
