package ru.citeck.ecos.action.group.exception;

/**
 * @author Pavel Simonov
 */
public class ManualCancelException extends GroupActionException {

    public ManualCancelException() {
    }

    public ManualCancelException(String message) {
        super(message);
    }

    public ManualCancelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManualCancelException(Throwable cause) {
        super(cause);
    }
}
