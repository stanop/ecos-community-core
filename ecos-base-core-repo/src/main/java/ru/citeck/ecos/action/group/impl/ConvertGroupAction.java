package ru.citeck.ecos.action.group.impl;

import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.GroupAction;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConvertGroupAction<Internal, Output> implements GroupAction<Output> {

    private GroupAction<Internal> internalAction;

    private Function<Internal, Output> toOut;
    private Function<Output, Internal> toIn;

    public ConvertGroupAction(GroupAction<Internal> internalAction,
                              Function<Internal, Output> toOut,
                              Function<Output, Internal> toIn) {
        this.internalAction = internalAction;
        this.toIn = toIn;
        this.toOut = toOut;
    }

    @Override
    public void process(Output nodeId) {
        Internal inNode = toIn.apply(nodeId);
        if (inNode != null) {
            internalAction.process(inNode);
        }
    }

    @Override
    public List<ActionResult<Output>> complete() {
        return convertToOut(internalAction.complete());
    }

    @Override
    public List<ActionResult<Output>> cancel() {
        return convertToOut(internalAction.cancel());
    }

    @Override
    public boolean isAsync() {
        return internalAction.isAsync();
    }

    @Override
    public long getTimeout() {
        return internalAction.getTimeout();
    }

    private List<ActionResult<Output>> convertToOut(List<ActionResult<Internal>> internal) {
        return internalAction.cancel()
                .stream()
                .map(r -> new ActionResult<>(toOut.apply(r.getData()), r.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConvertGroupAction<?, ?> that = (ConvertGroupAction<?, ?>) o;

        return Objects.equals(internalAction, that.internalAction);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(internalAction);
    }
}
