package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;

import java.util.Set;

public class ActivitiPerformTask extends ActivitiVariableScopeDelegate implements PerformTask {

    private final TaskEntity impl;

    public ActivitiPerformTask(TaskEntity impl) {
        super(impl);
        this.impl = impl;
    }

    @Override
    public String getAssignee() {
        return impl.getAssignee();
    }

    @Override
    public String getId() {
        return impl.getId();
    }

    @Override
    public Set<IdentityLink> getCandidates() {
        return impl.getCandidates();
    }

    @Override
    public String getProcessInstanceId() {
        return impl.getProcessInstanceId();
    }
}
