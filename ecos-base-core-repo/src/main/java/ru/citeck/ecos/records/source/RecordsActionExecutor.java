package ru.citeck.ecos.records.source;

import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.RecordRef;

import java.util.List;

public interface RecordsActionExecutor {

    ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config);

}
