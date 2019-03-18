package ru.citeck.ecos.records;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.predicate.PredicateService;
import ru.citeck.ecos.records.source.dao.*;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.meta.RecordsMetaService;
import ru.citeck.ecos.records2.source.dao.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecordsServiceImpl extends ru.citeck.ecos.records2.RecordsServiceImpl {

    private Map<String, RecordsActionExecutor> actionExecutors = new ConcurrentHashMap<>();

    public RecordsServiceImpl(RecordsMetaService recordsMetaService, PredicateService predicateService) {
        super(recordsMetaService, predicateService);
    }

    public ActionResults<RecordRef> executeAction(Collection<RecordRef> records,
                                                  GroupActionConfig processConfig) {

        ActionResults<RecordRef> results = new ActionResults<>();

        RecordsUtils.groupRefBySource(records).forEach((sourceId, refs) -> {

            Optional<RecordsActionExecutor> source = getRecordsDAO(sourceId, actionExecutors);

            if (source.isPresent()) {

                results.merge(source.get().executeAction(refs, processConfig));

            } else {

                ActionStatus status = ActionStatus.skipped("RecordsDAO can't execute action");
                results.addResults(refs.stream()
                                       .map(r -> new ActionResult<>(r, status))
                                       .collect(Collectors.toList()));
            }
        });
        return results;
    }

    @Override
    public void register(RecordsDAO recordsSource) {

        super.register(recordsSource);

        if (recordsSource instanceof RecordsActionExecutor) {
            actionExecutors.put(recordsSource.getId(), (RecordsActionExecutor) recordsSource);
        }
    }
}
