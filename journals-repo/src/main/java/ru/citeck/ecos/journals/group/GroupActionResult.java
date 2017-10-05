package ru.citeck.ecos.journals.group;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author Pavel Simonov
 */
public class GroupActionResult {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_SKIPPED = "SKIPPED";

    @Getter
    @Setter
    private String status = STATUS_OK;
    @Getter
    @Setter
    private String message = "";
    @Getter
    @Setter
    private String url;

    @Getter
    private Exception exception;

    public GroupActionResult() {

    }


    public GroupActionResult(String status) {
        this.status = status;
    }

    public void setException(Exception e) {
        this.exception = e;
        if (StringUtils.isBlank(message)) {
            this.message = e.getMessage();
        }
    }
}
