package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.model.BpmPackageModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleGrantWorkflowPackageListener implements TaskListener {

    private NodeService nodeService;
    private PermissionService permissionService;
    private String grantedPermission;

    @Override
    public void notify(DelegateTask task) {
        String permission = this.grantedPermission;
        if (permission != null) {
            List<NodeRef> nodeRefs = getNodeRefsList(task);
            if (nodeRefs.size() > 0) {
                Set<String> authorities = getTaskActors(task);
                if (authorities.size() > 0) {
                    for (NodeRef nodeRef : nodeRefs) {
                        for (String authority : authorities) {
                            AuthenticationUtil.runAsSystem(() -> {
                                permissionService.setPermission(nodeRef, authority, permission, true);
                                return null;
                            });
                        }
                    }
                }
            }
        }
    }

    private List<NodeRef> getNodeRefsList (DelegateTask task) {
        List<NodeRef> nodeRefs = new ArrayList<>();
        NodeRef wfPackage = ListenerUtils.getWorkflowPackage(task);

        if (nodeRefExists(wfPackage)) {
            nodeRefs.add(wfPackage);

            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(wfPackage,
                    BpmPackageModel.ASSOC_PACKAGE_CONTAINS,
                    RegexQNamePattern.MATCH_ALL);

            for (ChildAssociationRef childAssocRef : childAssocRefs) {
                NodeRef childRef = childAssocRef.getChildRef();
                if (nodeRefExists(childRef)) {
                    nodeRefs.add(childRef);
                }
            }
        }

        return nodeRefs;
    }

    private Set<String> getTaskActors(DelegateTask task) {
        Set<String> actors = new HashSet<String>();

        String actor = task.getAssignee();
        if (actor != null) {
            actors.add(actor);
        }

        Set<IdentityLink> candidates = ((TaskEntity)task).getCandidates();
        if (candidates != null) {
            for(IdentityLink candidate : candidates) {
                if (candidate.getGroupId() != null) {
                    actors.add(candidate.getGroupId());
                }
                if (candidate.getUserId() != null) {
                    actors.add(candidate.getUserId());
                }
            }
        }

        return actors;
    }

    private boolean nodeRefExists(NodeRef nodeRef) {
        return nodeRef != null && nodeService.exists(nodeRef);
    }

    public void setGrantedPermission(String grantedPermission) {
        this.grantedPermission = grantedPermission;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
