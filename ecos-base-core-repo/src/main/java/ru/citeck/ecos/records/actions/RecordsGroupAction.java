package ru.citeck.ecos.records.actions;

import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.impl.ResultsListener;
import ru.citeck.ecos.records.RecordInfo;

public interface RecordsGroupAction<T> extends GroupAction<RecordInfo<T>>,
                                               ResultsListener<RecordInfo<T>> {

    int REMOTE_BATCH_SIZE_DEFAULT = 20;

    default int getRemoteBatchSize() {
        return REMOTE_BATCH_SIZE_DEFAULT;
    }
}
