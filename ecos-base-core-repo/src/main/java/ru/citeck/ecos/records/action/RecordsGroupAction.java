package ru.citeck.ecos.records.action;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.impl.BaseGroupAction;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;

import java.util.*;

public class RecordsGroupAction extends BaseGroupAction<RecordRef> {

    private RecordsService recordsService;

    private Map<String, GroupAction<String>> sourceActions = new HashMap<>();

    private GroupActionConfig originalConfig;
    private String originalActionId;

    public RecordsGroupAction(GroupActionConfig config,
                              GroupActionConfig originalConfig,
                              String originalActionId,
                              RecordsService recordsService) {
        super(config);
        this.recordsService = recordsService;
        this.originalConfig = originalConfig;
        this.originalActionId = originalActionId;
    }

    @Override
    protected void onComplete(List<ActionResult<RecordRef>> output) {
        sourceActions.forEach((source, action) -> addResults(output, action.complete(), source));
    }

    @Override
    protected void processNodesImpl(List<RecordRef> nodes, List<ActionResult<RecordRef>> output) {
        for (RecordRef ref : nodes) {
            processRecord(ref);
        }
    }

    private void processRecord(RecordRef recordRef) {
        GroupAction<String> action = sourceActions.computeIfAbsent(recordRef.getId(), this::createSourceAction);
        action.process(recordRef.getId());
    }

    private void addResults(List<ActionResult<RecordRef>> output,
                            List<ActionResult<String>> results,
                            String sourceId) {

        for (ActionResult<String> result : results) {
            output.add(new ActionResult<>(new RecordRef(sourceId, result.getNodeId()), result.getStatus()));
        }
    }

    private GroupAction<String> createSourceAction(String sourceId) {
        return recordsService.createAction(sourceId, originalActionId, originalConfig);
    }
}
