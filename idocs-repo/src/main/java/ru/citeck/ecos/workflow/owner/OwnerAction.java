package ru.citeck.ecos.workflow.owner;

public enum OwnerAction {

    CLAIM("claim"),
    RELEASE("release");

    private final String action;

    OwnerAction(final String action) {
        this.action = action;
    }

    public String toString() {
        return action;
    }
}
