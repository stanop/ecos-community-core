package ru.citeck.ecos.action.group.exception;

public class GroupActionException extends RuntimeException {

    public GroupActionException() {
    }

    public GroupActionException(String message) {
        super(message);
    }

    public GroupActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupActionException(Throwable cause) {
        super(cause);
    }
}
