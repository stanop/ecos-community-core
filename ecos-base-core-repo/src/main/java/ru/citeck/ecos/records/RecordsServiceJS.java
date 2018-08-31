package ru.citeck.ecos.records;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionServiceJS;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class RecordsServiceJS extends AlfrescoScopableProcessorExtension {

    private RecordsService recordsService;

    public ActionResult<RecordRef>[] executeAction(Object nodes, String actionId, Object config) {
        Iterable<RecordRef> nodeRefs = GroupActionServiceJS.toIterableNodes(nodes);
        GroupActionConfig actionConfig = GroupActionServiceJS.convertConfig(config, GroupActionConfig.class);
        return GroupActionServiceJS.toArray(recordsService.executeAction(nodeRefs, actionId, actionConfig));
    }

    public ActionResult<RecordRef>[] executeAction(String sourceId, Object recordsQuery, String actionId, Object config) {
        RecordsQuery convertedQuery = GroupActionServiceJS.convertConfig(config, RecordsQuery.class);
        GroupActionConfig actionConfig = GroupActionServiceJS.convertConfig(config, GroupActionConfig.class);
        return GroupActionServiceJS.toArray(recordsService.executeAction(sourceId, convertedQuery, actionId, actionConfig));
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }
}
