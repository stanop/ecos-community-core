package ru.citeck.ecos.action.group;

public class ActionResult<T> {

    private final T nodeId;
    private final ActionStatus status;

    public ActionResult(T nodeId, String statusId) {
        this.nodeId = nodeId;
        this.status = new ActionStatus(statusId);
    }

    public ActionResult(T nodeId, ActionStatus status) {
        this.nodeId = nodeId;
        this.status = status;
    }

    public T getNodeId() {
        return nodeId;
    }

    public ActionStatus getStatus() {
        return status;
    }
}
