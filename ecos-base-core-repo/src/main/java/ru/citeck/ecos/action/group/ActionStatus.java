package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Pavel Simonov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionStatus {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_SKIPPED = "SKIPPED";
    public static final String STATUS_PERMISSION_DENIED = "PERMISSION_DENIED";

    @Getter @Setter
    private String key = STATUS_OK;
    @Getter
    private String message = "";
    @Getter @Setter
    private String url;

    @Setter
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                  include = JsonTypeInfo.As.WRAPPER_OBJECT)
    private Object data;

    @JsonIgnore
    @Getter
    private Exception exception;

    public ActionStatus() {
    }

    public ActionStatus(String statusKey) {
        this.key = statusKey;
    }

    public void setMessage(String msg) {
        if (msg != null) {
            String message = I18NUtil.getMessage(msg);
            this.message = message != null ? message : msg;
        } else {
            this.message = null;
        }
    }

    @JsonIgnore
    public boolean isOk() {
        return ActionStatus.STATUS_OK.equals(key);
    }

    @JsonIgnore
    public boolean isSkipped() {
        return ActionStatus.STATUS_SKIPPED.equals(key);
    }

    @JsonIgnore
    public boolean isError() {
        return ActionStatus.STATUS_ERROR.equals(key);
    }

    @JsonIgnore
    public static ActionStatus ok() {
        return new ActionStatus(ActionStatus.STATUS_OK);
    }

    @JsonIgnore
    public static ActionStatus ok(Object data) {
        ActionStatus status = ok();
        status.setData(data);
        return status;
    }

    @JsonIgnore
    public static ActionStatus ok(String message) {
        ActionStatus status = ok();
        status.setMessage(message);
        return status;
    }

    @JsonIgnore
    public static ActionStatus error(Exception e) {
        ActionStatus status = new ActionStatus(STATUS_ERROR);
        status.setException(e);
        return status;
    }

    @JsonIgnore
    public static ActionStatus skipped() {
        return new ActionStatus(STATUS_SKIPPED);
    }

    @JsonIgnore
    public static ActionStatus skipped(String message) {
        ActionStatus status = skipped();
        status.setMessage(message);
        return status;
    }

    public <T> T getData() {
        return (T) data;
    }

    public void setException(Exception e) {
        if (e == null) {
            return;
        }
        this.exception = e;
        if (StringUtils.isBlank(message)) {
            setMessage(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "ActionStatus{" +
                "key='" + key + '\'' +
                ", message='" + message + '\'' +
                ", url='" + url +
                "\'}";
    }
}
