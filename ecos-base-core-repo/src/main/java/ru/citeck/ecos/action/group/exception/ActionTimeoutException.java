package ru.citeck.ecos.action.group.exception;

/**
 * @author Pavel Simonov
 */
public class ActionTimeoutException extends GroupActionException {

    public ActionTimeoutException() {
    }

    public ActionTimeoutException(String message) {
        super(message);
    }

    public ActionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionTimeoutException(Throwable cause) {
        super(cause);
    }
}
