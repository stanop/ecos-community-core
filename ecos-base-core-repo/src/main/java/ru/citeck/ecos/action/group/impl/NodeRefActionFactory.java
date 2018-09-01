package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionFactory;
import ru.citeck.ecos.records.RecordRef;

public abstract class NodeRefActionFactory implements GroupActionFactory<RecordRef> {

    @Override
    public final GroupAction<RecordRef> createAction(GroupActionConfig config) {
        GroupAction<NodeRef> action = createNodeRefAction(config);
        return new ConvertGroupAction<>(action, RecordRef::new, this::convertToNodeRef);
    }

    @Override
    public Class<RecordRef> getActionType() {
        return RecordRef.class;
    }

    protected abstract GroupAction<NodeRef> createNodeRefAction(GroupActionConfig config);

    private NodeRef convertToNodeRef(RecordRef record) {
        if (!record.getSourceId().isEmpty()) {
            return NodeRef.isNodeRef(record.getId()) ? new NodeRef(record.getId()) : null;
        }
        return null;
    }
}
