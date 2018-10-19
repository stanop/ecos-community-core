package ru.citeck.ecos.flowable.activiti.delegates;

import org.activiti.engine.task.IdentityLink;

public class ActivitiIdentityLink implements IdentityLink {

    private final org.flowable.identitylink.api.IdentityLink impl;

    public ActivitiIdentityLink(org.flowable.identitylink.api.IdentityLink impl) {
        this.impl = impl;
    }

    @Override
    public String getType() {
        return impl.getType();
    }

    @Override
    public String getUserId() {
        return impl.getUserId();
    }

    @Override
    public String getGroupId() {
        return impl.getGroupId();
    }

    @Override
    public String getTaskId() {
        return impl.getTaskId();
    }

    @Override
    public String getProcessDefinitionId() {
        return impl.getProcessDefinitionId();
    }

    @Override
    public String getProcessInstanceId() {
        return impl.getProcessInstanceId();
    }
}
