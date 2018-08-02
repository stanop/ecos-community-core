package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;

public class ActionExecution {

    private Iterable<RemoteRef> nodes;
    private GroupAction action;
    private boolean cancelled = false;

    ActionExecution(Iterable<RemoteRef> nodes, GroupAction action) {
        this.nodes = nodes;
        this.action = action;
    }

    List<ActionResult> run() {
        for (RemoteRef ref : nodes) {
            action.process(ref);
            if (cancelled) {
                break;
            }
        }
        return cancelled ? action.cancel() : action.complete();
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActionExecution that = (ActionExecution) o;

        return nodes.equals(that.nodes) && action.equals(that.action);
    }

    @Override
    public int hashCode() {
        int result = nodes.hashCode();
        result = 31 * result + action.hashCode();
        return result;
    }
}
