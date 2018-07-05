package ru.citeck.ecos.form.action.handlers;

import ru.citeck.ecos.form.action.FormActionHandlerProvider;

public abstract class AbstractFormActionHandler implements FormActionHandler {

    protected int order;
    protected String taskType;
    private FormActionHandlerProvider formActionHandlerProvider;

    public void init() {
        formActionHandlerProvider.subscribe(this);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setFormActionHandlerProvider(FormActionHandlerProvider formActionHandlerProvider) {
        this.formActionHandlerProvider = formActionHandlerProvider;
    }
}
