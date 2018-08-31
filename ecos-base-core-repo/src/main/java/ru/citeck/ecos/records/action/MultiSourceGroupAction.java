package ru.citeck.ecos.records.action;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.impl.BaseGroupAction;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.util.*;
import java.util.function.Function;

public class MultiSourceGroupAction extends BaseGroupAction<RecordRef> {

    private Map<String, GroupAction<String>> sourceActions = new HashMap<>();

    private GroupActionConfig originalConfig;
    private String originalActionId;

    private Function<String, RecordsDAO> getDaoBySource;

    public MultiSourceGroupAction(GroupActionConfig config,
                                  GroupActionConfig originalConfig,
                                  String originalActionId,
                                  Function<String, RecordsDAO> getDaoBySource) {
        super(config);
        this.originalConfig = originalConfig;
        this.originalActionId = originalActionId;
        this.getDaoBySource = getDaoBySource;
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
        String sourceId = recordRef.getSourceId();
        GroupAction<String> action = sourceActions.computeIfAbsent(sourceId, this::createSourceAction);
        action.process(recordRef.getId());
    }

    private void addResults(List<ActionResult<RecordRef>> output,
                            List<ActionResult<String>> results,
                            String sourceId) {

        for (ActionResult<String> result : results) {
            RecordRef recordId = new RecordRef(sourceId, result.getNodeId());
            output.add(new ActionResult<>(recordId, result.getStatus()));
        }
    }

    private GroupAction<String> createSourceAction(String sourceId) {
        RecordsDAO source = getDaoBySource.apply(sourceId);
        return source.createAction(originalActionId, originalConfig);
    }
}
