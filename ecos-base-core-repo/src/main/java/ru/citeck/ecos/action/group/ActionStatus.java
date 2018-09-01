package ru.citeck.ecos.action.group;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author Pavel Simonov
 */
public class ActionStatus {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_SKIPPED = "SKIPPED";

    @Getter
    @Setter
    private String key = STATUS_OK;
    @Getter
    @Setter
    private String message = "";
    @Getter
    @Setter
    private String url;

    @Getter
    private Exception exception;

    public ActionStatus() {
    }

    public ActionStatus(String statusKey) {
        this.key = statusKey;
    }

    public void setException(Exception e) {
        this.exception = e;
        if (StringUtils.isBlank(message)) {
            this.message = e.getMessage();
        }
    }
}
