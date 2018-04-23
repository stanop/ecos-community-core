package ru.citeck.ecos.utils.performance;

public class ActionPerformance  extends Performance {

    private String actionName;

    public ActionPerformance(Object instance, String actionName) {
        super(instance);
        this.actionName = actionName;
    }

    public void restart(String actionName) {
        this.actionName = actionName;
        super.restart();
    }

    @Override
    public String toString() {
        return actionName;
    }
}
