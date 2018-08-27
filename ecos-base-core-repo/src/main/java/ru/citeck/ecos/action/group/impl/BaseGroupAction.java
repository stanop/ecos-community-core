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
    private int errorsCount = 0;

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
        onComplete();
        return output;
    }

    @Override
    public List<ActionResult> cancel() {
        onCancel();
        return output;
    }

    @Override
    public boolean isAsync() {
        return config.isAsync();
    }

    @Override
    public long getTimeout() {
        return config.getTimeout();
    }

    private void processNodes() {
        List<ActionResult> results = new ArrayList<>();
        processNodesImpl(input, results);
        input.clear();
        for (ActionResult result : results) {
            ActionStatus status = result.getStatus();
            if (status != null && ActionStatus.STATUS_ERROR.equals(status.getKey())) {
                errorsCount++;
            }
            output.add(result);
        }
        int maxErrors = config.getMaxErrors();
        if (maxErrors > 0 && errorsCount >= maxErrors) {
            throw new RuntimeException("Group action max errors limit is reached! " + toString());
        }
    }

    protected void onComplete() {
    }

    protected void onCancel() {
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

    public GroupActionConfig getConfig() {
         return config;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseGroupAction that = (BaseGroupAction) o;

        return Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(config);
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
                " config: " + config + " errors: " + errorsCount;
    }
}
