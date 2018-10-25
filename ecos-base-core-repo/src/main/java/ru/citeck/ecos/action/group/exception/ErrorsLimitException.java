package ru.citeck.ecos.action.group.exception;

/**
 * @author Pavel Simonov
 */
public class ErrorsLimitException extends GroupActionException {

    public ErrorsLimitException() {
    }

    public ErrorsLimitException(String message) {
        super(message);
    }

    public ErrorsLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
