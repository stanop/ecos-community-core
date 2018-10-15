package ru.citeck.ecos.action.group.impl;

/**
 * @author Pavel Simonov
 */
public class ErrorsLimitReachedException extends RuntimeException {

    public ErrorsLimitReachedException() {
    }

    public ErrorsLimitReachedException(String message) {
        super(message);
    }

    public ErrorsLimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
