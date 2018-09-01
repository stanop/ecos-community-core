package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResult<T> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                  property = "dataType")
    private T data;
    private ActionStatus status;

    public ActionResult() {
    }

    public ActionResult(T data, String statusId) {
        this.data = data;
        this.status = new ActionStatus(statusId);
    }

    public ActionResult(T data, ActionStatus status) {
        this.data = data;
        this.status = status;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus actionStatus) {
        status = actionStatus;
    }
}
