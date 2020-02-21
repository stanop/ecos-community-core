package ru.citeck.ecos.workflow.listeners;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.workflow.listeners.model.TaskListenerDocumentInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TaskDataListenerUtils {

    private static final String TITLE_DELIMITER = "|";

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
            eventProperties.put(HistoryModel.PROP_DOC_STATUS_TITLE, getTitleData(documentMeta));

        }
    }

    //TODO: rewrite to normal format?
    private static String getTitleData(TaskListenerDocumentInfo documentMeta) {
        List<String> titles = new ArrayList<>();
        titles.add(documentMeta.getStatusTitleEn());
        titles.add(documentMeta.getStatusTitleRu());

        return titles.stream()
            .filter(StringUtils::isNoneBlank)
            .collect(Collectors.joining(TITLE_DELIMITER));
    }
}
