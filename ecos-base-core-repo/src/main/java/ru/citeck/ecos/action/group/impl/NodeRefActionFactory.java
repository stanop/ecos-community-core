package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionFactory;
import ru.citeck.ecos.action.group.impl.ConvertGroupAction;

public abstract class NodeRefActionFactory implements GroupActionFactory<String> {

    @Override
    public final GroupAction<String> createAction(GroupActionConfig config) {
        GroupAction<NodeRef> action = createNodeRefAction(config);
        return new ConvertGroupAction<>(action, Object::toString, this::convertToNodeRef);
    }

    protected abstract GroupAction<NodeRef> createNodeRefAction(GroupActionConfig config);

    private NodeRef convertToNodeRef(String id) {
        return NodeRef.isNodeRef(id) ? new NodeRef(id) : null;
    }
}
