package ru.citeck.ecos.action.group.exception;

public class ElementsLimitException extends GroupActionException {

    public ElementsLimitException() {
    }

    public ElementsLimitException(String message) {
        super(message);
    }

    public ElementsLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElementsLimitException(Throwable cause) {
        super(cause);
    }
}
