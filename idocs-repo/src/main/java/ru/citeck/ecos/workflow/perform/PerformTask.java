package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.task.IdentityLink;

import java.util.Set;

public interface PerformTask extends VariableScope {

    String getAssignee();

    String getId();

    Set<IdentityLink> getCandidates();

    String getProcessInstanceId();
}
