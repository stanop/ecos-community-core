package ru.citeck.ecos.action.group.impl;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public abstract class BaseGroupAction implements GroupAction {

    private final List<RemoteRef> input = new ArrayList<>();
    private final List<ActionResult> output = new ArrayList<>();

    protected final GroupActionConfig config;

    public BaseGroupAction(GroupActionConfig config) {
        this.config = config != null ? config : new GroupActionConfig();
    }

    @Override
    public final void process(RemoteRef remoteRef) {
        input.add(remoteRef);
        int batchSize = config.getBatchSize();
        if (batchSize > 0 && input.size() >= batchSize) {
            processNodes();
        }
    }

    @Override
    public final List<ActionResult> complete() {
        if (input.size() > 0) {
            processNodes();
        }
        return output;
    }

    @Override
    public boolean isAsync() {
        return config.isAsync();
    }

    private void processNodes() {
        processNodesImpl(input, output);
        input.clear();
    }

    protected void processNodesImpl(List<RemoteRef> nodes, List<ActionResult> output) {
        for (RemoteRef node : nodes) {
            ActionStatus status = processImpl(node);
            if (output.size() < config.getMaxResults()) {
                output.add(new ActionResult(node, status));
            }
        }
    }

    protected ActionStatus processImpl(RemoteRef nodeRef) {
        throw new RuntimeException("Method not implemented");
    }
}
