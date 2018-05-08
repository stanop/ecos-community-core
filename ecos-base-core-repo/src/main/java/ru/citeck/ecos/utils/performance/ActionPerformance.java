package ru.citeck.ecos.utils.performance;

import org.apache.commons.lang.StringUtils;

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
        if (StringUtils.isNotBlank(actionName)) {
            return actionName;
        }
        return String.format("Thread[%s]", Thread.currentThread().getName());
    }
}
