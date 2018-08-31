package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class ActionResult<T> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                  property = "idType")
    private T nodeId;
    private ActionStatus status;

    public ActionResult() {
    }

    public ActionResult(T nodeId, String statusId) {
        this.nodeId = nodeId;
        this.status = new ActionStatus(statusId);
    }

    public ActionResult(T nodeId, ActionStatus status) {
        this.nodeId = nodeId;
        this.status = status;
    }

    public void setNodeId(T nodeId) {
        this.nodeId = nodeId;
    }

    public T getNodeId() {
        return nodeId;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus actionStatus) {
        status = actionStatus;
    }
}
