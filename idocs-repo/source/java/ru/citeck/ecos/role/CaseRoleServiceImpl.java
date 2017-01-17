package ru.citeck.ecos.role;

import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.ParameterCheck;
import ru.citeck.ecos.role.CaseRolePolicies.OnRoleAssigneesChangedPolicy;
import ru.citeck.ecos.role.CaseRolePolicies.OnCaseRolesAssigneesChangedPolicy;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.role.dao.RoleDAO;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

/**
 * @author Maxim Strizhov
 * @author Pavel Simonov
 */
public class CaseRoleServiceImpl implements CaseRoleService {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;

    private Map<QName, RoleDAO> rolesDAOByType = new HashMap<>();

    private ClassPolicyDelegate<OnRoleAssigneesChangedPolicy> onRoleAssigneesChangedDelegate;
    private ClassPolicyDelegate<OnCaseRolesAssigneesChangedPolicy> onCaseRolesAssigneesChangedDelegate;

    public void init() {
        onRoleAssigneesChangedDelegate = policyComponent.registerClassPolicy(OnRoleAssigneesChangedPolicy.class);
        onCaseRolesAssigneesChangedDelegate = policyComponent.registerClassPolicy(OnCaseRolesAssigneesChangedPolicy.class);
    }

    @Override
    public List<NodeRef> getRoles(NodeRef caseRef) {
        if (caseRef == null || !nodeService.exists(caseRef)) {
            return Collections.emptyList();
        }
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(caseRef, ICaseRoleModel.ASSOC_ROLES,
                                                                               RegexQNamePattern.MATCH_ALL);
        List<NodeRef> result = new ArrayList<>(assocs.size());
        for (ChildAssociationRef assoc : assocs) {
            result.add(assoc.getChildRef());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public NodeRef getRole(NodeRef caseRef, String name) {
        ParameterCheck.mandatoryString("name", name);

        List<NodeRef> roles = getRoles(caseRef);
        for (NodeRef roleRef : roles) {
            String varName = (String) nodeService.getProperty(roleRef, ICaseRoleModel.PROP_VARNAME);
            if (name.equals(varName)) {
                return roleRef;
            }
        }
        return null;
    }

    @Override
    public void setAssignees(NodeRef caseRef, String roleName, Collection<NodeRef> assignees) {
        setAssignees(needRole(caseRef, roleName), assignees);
    }

    @Override
    public void setAssignees(NodeRef roleRef, Collection<NodeRef> assignees) {
        if (assignees == null || assignees.isEmpty()) {
            removeAssignees(roleRef);
            return;
        }
        Set<NodeRef> existing = getAssignees(roleRef);
        Set<NodeRef> added = subtract(assignees, existing);
        Set<NodeRef> removed = subtract(existing, assignees);
        for (NodeRef assignee : added) {
            nodeService.createAssociation(roleRef, assignee, ICaseRoleModel.ASSOC_ASSIGNEES);
        }
        for (NodeRef assignee : removed) {
            nodeService.removeAssociation(roleRef, assignee, ICaseRoleModel.ASSOC_ASSIGNEES);
        }
        fireAssigneesChangedEvent(roleRef, added, removed);
    }

    @Override
    public void addAssignees(NodeRef caseRef, String roleName, NodeRef... assignees) {
        addAssignees(needRole(caseRef, roleName), assignees);
    }

    @Override
    public void addAssignees(NodeRef roleRef, NodeRef... assignees) {
        addAssignees(roleRef, Arrays.asList(assignees));
    }

    @Override
    public void addAssignees(NodeRef caseRef, String roleName, Collection<NodeRef> assignees) {
        addAssignees(needRole(caseRef, roleName), assignees);
    }

    @Override
    public void addAssignees(NodeRef roleRef, Collection<NodeRef> assignees) {
        if (assignees == null || assignees.isEmpty()) {
            return;
        }
        Set<NodeRef> existing = getAssignees(roleRef);
        Set<NodeRef> added = subtract(assignees, existing);
        for (NodeRef assignee : added) {
            nodeService.createAssociation(roleRef, assignee, ICaseRoleModel.ASSOC_ASSIGNEES);
        }
        fireAssigneesChangedEvent(roleRef, added, null);
    }

    @Override
    public Set<NodeRef> getAssignees(NodeRef caseRef, String roleName) {
        return getAssignees(needRole(caseRef, roleName));
    }

    @Override
    public Set<NodeRef> getAssignees(NodeRef roleRef) {
        return getTargets(roleRef, ICaseRoleModel.ASSOC_ASSIGNEES);
    }

    @Override
    public void removeAssignees(NodeRef caseRef, String roleName) {
        removeAssignees(needRole(caseRef, roleName));
    }

    @Override
    public void removeAssignees(NodeRef roleRef) {
        Set<NodeRef> assignees = getTargets(roleRef, ICaseRoleModel.ASSOC_ASSIGNEES);
        for (NodeRef ref : assignees) {
            nodeService.removeAssociation(roleRef, ref, ICaseRoleModel.ASSOC_ASSIGNEES);
        }
        fireAssigneesChangedEvent(roleRef, null, assignees);
    }

    @Override
    public boolean isRoleMember(NodeRef caseRef, String roleName, NodeRef authorityRef) {
        return isRoleMember(needRole(caseRef, roleName), authorityRef);
    }

    @Override
    public boolean isRoleMember(NodeRef caseRef, String roleName, NodeRef authorityRef, boolean immediate) {
        return isRoleMember(needRole(caseRef, roleName), authorityRef, immediate);
    }

    @Override
    public boolean isRoleMember(NodeRef roleRef, NodeRef authorityRef) {
        return isRoleMember(roleRef, authorityRef, false);
    }

    @Override
    public boolean isRoleMember(NodeRef roleRef, NodeRef authorityRef, boolean immediate) {

        Set<NodeRef> assignees = getAssignees(roleRef);

        if (assignees.contains(authorityRef)) {
            return true;
        } else if (immediate) {
            return false;
        }

        String authorityName = RepoUtils.getAuthorityName(authorityRef, nodeService, dictionaryService);
        AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);

        for (NodeRef assigneeRef : assignees) {
            String assigneeName = RepoUtils.getAuthorityName(assigneeRef, nodeService, dictionaryService);
            Set<String> authorities = authorityService.getContainedAuthorities(authorityType, assigneeName, false);
            if (authorities.contains(authorityName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateRoles(NodeRef caseRef) {
        Collection<NodeRef> roles = getRoles(caseRef);
        for (NodeRef roleRef : roles) {
            updateRole(caseRef, roleRef);
        }
    }

    @Override
    public void updateRole(NodeRef caseRef, String roleName) {
        updateRole(caseRef, needRole(caseRef, roleName));
    }

    @Override
    public void updateRole(NodeRef roleRef) {
        NodeRef caseRef = nodeService.getPrimaryParent(roleRef).getParentRef();
        updateRole(caseRef, roleRef);
    }

    @Override
    public void register(RoleDAO roleDAO) {
        rolesDAOByType.put(roleDAO.getRoleType(), roleDAO);
    }

    private void updateRole(NodeRef caseRef, NodeRef roleRef) {
        QName type = nodeService.getType(roleRef);
        RoleDAO dao = rolesDAOByType.get(type);
        if (dao != null) {
            setAssignees(roleRef, dao.getAssignees(caseRef, roleRef));
        }
    }

    private void fireAssigneesChangedEvent(NodeRef roleRef, Set<NodeRef> added, Set<NodeRef> removed) {
        if (added == null) {
            added = Collections.emptySet();
        }
        if (removed == null) {
            removed = Collections.emptySet();
        }
        if (added.isEmpty() && removed.isEmpty()) {
            return;
        }
        Set<QName> classes;

        classes = new HashSet<>(DictionaryUtils.getNodeClassNames(roleRef, nodeService));
        OnRoleAssigneesChangedPolicy changedPolicy = onRoleAssigneesChangedDelegate.get(roleRef, classes);
        changedPolicy.onRoleAssigneesChanged(roleRef, added, removed);

        NodeRef caseRef = nodeService.getPrimaryParent(roleRef).getParentRef();
        classes = new HashSet<>(DictionaryUtils.getNodeClassNames(caseRef, nodeService));
        OnCaseRolesAssigneesChangedPolicy rolesChangedPolicy = onCaseRolesAssigneesChangedDelegate.get(caseRef, classes);
        rolesChangedPolicy.onCaseRolesAssigneesChanged(caseRef);
    }

    private Set<NodeRef> getTargets(NodeRef nodeRef, QName assocType) {
        List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, assocType);
        Set<NodeRef> result = new HashSet<>();
        for (AssociationRef ref : assocs) {
            result.add(ref.getTargetRef());
        }
        return result;
    }

    private Set<NodeRef> subtract(Collection<NodeRef> from, Collection<NodeRef> values) {
        if (from == null || from.isEmpty()) {
            return Collections.emptySet();
        }
        Set<NodeRef> result = new HashSet<>(from);
        if (values != null) {
            result.removeAll(values);
        }
        return Collections.unmodifiableSet(result);
    }

    private NodeRef needRole(NodeRef caseRef, String name) {
        NodeRef roleRef = getRole(caseRef, name);
        if (roleRef == null) {
            throw new IllegalArgumentException("Role with name '" + name + "' not found in case " + caseRef);
        }
        return roleRef;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
