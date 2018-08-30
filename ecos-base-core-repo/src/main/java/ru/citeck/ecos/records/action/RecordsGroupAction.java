package ru.citeck.ecos.records.action;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.impl.BaseGroupAction;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;

import java.util.*;

public class RecordsGroupAction extends BaseGroupAction<RecordRef> {

    private RecordsService recordsService;

    private Map<String, Set<String>> recordsBySource = new HashMap<>();

    public RecordsGroupAction(GroupActionConfig config, RecordsService recordsService) {
        super(config);
        this.recordsService = recordsService;
    }

    @Override
    protected void onComplete(List<ActionResult<RecordRef>> output) {
        recordsBySource.forEach((source, records) -> {
            if (records.size() > 0) {
                processRecords(source, records, output);
            }
        });
    }

    @Override
    protected void processNodesImpl(List<RecordRef> nodes, List<ActionResult<RecordRef>> output) {
        for (RecordRef ref : nodes) {
            Set<String> records = recordsBySource.computeIfAbsent(ref.getSourceId(), key -> new HashSet<>());
            records.add(ref.getId());
            if (records.size() >= config.getBatchSize()) {
                processRecords(ref.getSourceId(), records, output);
                records.clear();
            }
        }
    }

    private void processRecords(String source, Set<String> records, List<ActionResult<RecordRef>> output) {

    }
}
