package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.impl.persistence.entity.TaskEntity;
import org.flowable.engine.task.IdentityLink;
import ru.citeck.ecos.flowable.listeners.global.GlobalAssignmentTaskListener;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Assign task to single candidate listener
 */
public class AssignTaskToSingleCandidateListener implements GlobalCreateTaskListener, GlobalAssignmentTaskListener {

    /**
     * Authority service
     */
    private AuthorityService authorityService;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        if(delegateTask.getAssignee() != null) {
            return;
        }
        Set<IdentityLink> candidates = ((TaskEntity) delegateTask).getCandidates();
        Set<String> userNames = getCandidateUsers(candidates);
        if(userNames.size() == 1) {
            delegateTask.setAssignee(userNames.iterator().next());
        }
    }

    /**
     * Get candidate users
     * @param candidates Set of candidates
     * @return Set of username
     */
    private Set<String> getCandidateUsers(Set<IdentityLink> candidates) {
        Set<String> userNames = new HashSet<String>();
        for(IdentityLink candidate : candidates) {
            String groupId = candidate.getGroupId(),
                    userId = candidate.getUserId();
            if(groupId != null) {
                Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, groupId, false);
                userNames.addAll(users);
            }
            if(userId != null) {
                userNames.add(userId);
            }
        }
        return userNames;
    }

    /**
     * Set authority service
     * @param authorityService Authority service
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
