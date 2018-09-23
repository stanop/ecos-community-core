package ru.citeck.ecos.action.group.impl;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.io.IOException;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public abstract class BaseGroupAction<T> implements GroupAction<T> {

    private final List<T> input = new ArrayList<>();
    private final List<ActionResult<T>> output = new ArrayList<>();

    protected final GroupActionConfig config;
    private int errorsCount = 0;

    private List<ResultsListener<T>> listeners = new ArrayList<>();

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
        onComplete();
        return output;
    }

    @Override
    public List<ActionResult<T>> cancel() {
        onCancel();
        return output;
    }

    @Override
    public void onError(Throwable error) {
    }

    @Override
    public boolean isAsync() {
        return config.isAsync();
    }

    @Override
    public long getTimeout() {
        return config.getTimeout();
    }

    @Override
    public void addListener(ResultsListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void onProcessed(List<ActionResult<T>> results) {

        listeners.forEach(l -> l.onProcessed(results));

        for (ActionResult<T> result : results) {
            ActionStatus status = result.getStatus();
            if (status != null && ActionStatus.STATUS_ERROR.equals(status.getKey())) {
                errorsCount++;
            }
            if (config.getMaxResults() > output.size()) {
                output.add(result);
            }
        }
        int maxErrors = config.getMaxErrors();
        if (maxErrors > 0 && errorsCount >= maxErrors) {
            throw new RuntimeException("Group action max errors limit is reached! " + toString());
        }
    }

    private void processNodes() {
        processNodesImpl(input);
        input.clear();
    }

    protected void onComplete() {
    }

    protected void onCancel() {
    }

    protected void processNodesImpl(List<T> nodes) {
        List<ActionResult<T>> results = new ArrayList<>();
        for (T node : nodes) {
            ActionStatus status = processImpl(node);
            results.add(new ActionResult<>(node, status));
        }
        onProcessed(results);
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
