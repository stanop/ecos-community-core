package ru.citeck.ecos.records.action;

import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionFactory;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;

public class RecordsGroupActionFactory implements GroupActionFactory<RecordRef> {

    public static final String ID = "recordsAction";
    public static final String ACTION_CONFIG = "actionConfig";

    private RecordsService recordsService;

    @Override
    public GroupAction<RecordRef> createAction(GroupActionConfig config) {
        return new RecordsGroupAction(config, recordsService);
    }

    @Override
    public String getActionId() {
        return ID;
    }
}
