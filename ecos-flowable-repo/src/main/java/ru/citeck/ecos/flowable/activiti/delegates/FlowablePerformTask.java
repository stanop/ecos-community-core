package ru.citeck.ecos.flowable.activiti.delegates;

import org.activiti.engine.task.IdentityLink;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import ru.citeck.ecos.workflow.perform.PerformTask;

import java.util.HashSet;
import java.util.Set;

public class FlowablePerformTask extends FlowableVariableScopeDelegate implements PerformTask {

    private TaskEntity impl;

    public FlowablePerformTask(TaskEntity impl) {
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
        Set<IdentityLink> links = new HashSet<>();
        Set<org.flowable.identitylink.api.IdentityLink> candidates = impl.getCandidates();
        if (candidates != null) {
            candidates.forEach(link -> links.add(new ActivitiIdentityLink(link)));
        }
        return links;
    }

    @Override
    public String getProcessInstanceId() {
        return impl.getProcessInstanceId();
    }
}
