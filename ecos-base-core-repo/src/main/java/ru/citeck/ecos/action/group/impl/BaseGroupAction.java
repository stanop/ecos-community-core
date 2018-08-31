package ru.citeck.ecos.action.group.impl;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public abstract class BaseGroupAction<T> implements GroupAction<T> {

    private final List<T> input = new ArrayList<>();
    private final List<ActionResult<T>> output = new ArrayList<>();

    protected final GroupActionConfig config;
    private int errorsCount = 0;

    public BaseGroupAction(GroupActionConfig config) {
        this.config = config != null ? config : new GroupActionConfig();
    }

    @Override
    public final void process(T remoteRef) {
        input.add(remoteRef);
        int batchSize = config.getBatchSize();
        if (batchSize > 0 && input.size() >= batchSize) {
            processNodes();
        }
    }

    @Override
    public final List<ActionResult<T>> complete() {
        if (input.size() > 0) {
            processNodes();
        }
        onComplete(output);
        return output;
    }

    @Override
    public List<ActionResult<T>> cancel() {
        onCancel(output);
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
        List<ActionResult<T>> results = new ArrayList<>();
        processNodesImpl(input, results);
        input.clear();
        for (ActionResult<T> result : results) {
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

    protected void onComplete(List<ActionResult<T>> output) {
    }

    protected void onCancel(List<ActionResult<T>> output) {
    }

    protected void processNodesImpl(List<T> nodes, List<ActionResult<T>> output) {
        for (T node : nodes) {
            ActionStatus status = processImpl(node);
            if (output.size() < config.getMaxResults()) {
                output.add(new ActionResult<>(node, status));
            }
        }
    }

    protected ActionStatus processImpl(T nodeRef) {
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
