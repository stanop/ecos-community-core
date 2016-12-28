package ru.citeck.ecos.role;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.springframework.extensions.surf.util.ParameterCheck;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim Strizhov
 * @author Pavel Simonov
 */
public class CaseRoleServiceJS extends AlfrescoScopableProcessorExtension {

    private final ValueConverter converter = new ValueConverter();

    private CaseRoleService caseRoleService;
    private AuthorityService authorityService;

    public ScriptNode getRole(Object document, String name) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        return JavaScriptImplUtils.wrapNode(caseRoleService.getRole(docRef, name), this);
    }

    public ScriptNode[] getRoles(Object document) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        List<NodeRef> roles = caseRoleService.getRoles(docRef);
        return JavaScriptImplUtils.wrapNodes(roles, this);
    }

    public void setAssignees(Object document, Object role, Serializable assignees) {
        setAssignees(getRoleRef(document, role), assignees);
    }

    public void setAssignees(Object role, Serializable assignees) {
        ParameterCheck.mandatory("role", role);
        ParameterCheck.mandatory("assignees", assignees);

        NodeRef roleRef = JavaScriptImplUtils.getNodeRef(role);
        Object assigneesArg = converter.convertValueForJava(assignees);

        if (!(assigneesArg instanceof List)) {
            throw new AlfrescoRuntimeException("Argument 'assignees' has wrong type: " + assignees.getClass()
                                                                      + ", expected: " + List.class);
        }
        List assigneeObjects = (List) assigneesArg;

        if (assigneeObjects.size() == 0) {
            caseRoleService.removeAssignees(roleRef);
        } else {
            List<NodeRef> assigneeRefs = new ArrayList<>();
            for (Object assignee : assigneeObjects) {
                assigneeRefs.add(getAuthorityRef(assignee));
            }
            caseRoleService.setAssignees(roleRef, assigneeRefs);
        }
    }

    public ScriptNode[] getAssignees(Object document, Object role) {
        NodeRef roleRef = getRoleRef(document, role);
        Set<NodeRef> assignees = caseRoleService.getAssignees(roleRef);
        return JavaScriptImplUtils.wrapNodes(assignees, this);
    }

    public ScriptNode[] getAssignees(Object role) {
        NodeRef roleRef = JavaScriptImplUtils.getNodeRef(role);
        Set<NodeRef> assignees = caseRoleService.getAssignees(roleRef);
        return JavaScriptImplUtils.wrapNodes(assignees, this);
    }

    public void updateRoles(Object document) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        caseRoleService.updateRoles(docRef);
    }

    public void updateRole(Object role) {
        NodeRef roleRef = JavaScriptImplUtils.getNodeRef(role);
        caseRoleService.updateRole(roleRef);
    }

    public void updateRole(Object document, Object role) {
        NodeRef roleRef = getRoleRef(document, role);
        caseRoleService.updateRole(roleRef);
    }

    public void removeAssignees(Object document, Object role) {
        caseRoleService.removeAssignees(getRoleRef(document, role));
    }

    public void removeAssignees(Object role) {
        caseRoleService.removeAssignees(JavaScriptImplUtils.getNodeRef(role));
    }

    public boolean isRoleMember(Object role, Object authority) {
        NodeRef roleRef = JavaScriptImplUtils.getNodeRef(role);
        NodeRef authorityRef = getAuthorityRef(authority);
        return caseRoleService.isRoleMember(roleRef, authorityRef);
    }

    public boolean isRoleMember(Object document, Object role, Object authority) {
        NodeRef roleRef = getRoleRef(document, role);
        NodeRef authorityRef = getAuthorityRef(authority);
        return caseRoleService.isRoleMember(roleRef, authorityRef);
    }

    public boolean isRoleMember(Object role, Object authority, boolean immediate) {
        NodeRef roleRef = JavaScriptImplUtils.getNodeRef(role);
        NodeRef authorityRef = getAuthorityRef(authority);
        return caseRoleService.isRoleMember(roleRef, authorityRef, immediate);
    }

    public boolean isRoleMember(Object document, Object role, Object authority, boolean immediate) {
        NodeRef roleRef = getRoleRef(document, role);
        NodeRef authorityRef = getAuthorityRef(authority);
        return caseRoleService.isRoleMember(roleRef, authorityRef, immediate);
    }

    private NodeRef getRoleRef(Object document, Object role) {
        if (role instanceof String) {
            String roleStr = (String) role;
            if (NodeRef.isNodeRef(roleStr)) {
                return new NodeRef(roleStr);
            }
            NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
            return caseRoleService.getRole(docRef, roleStr);
        }
        return JavaScriptImplUtils.getNodeRef(role);
    }

    private NodeRef getAuthorityRef(Object assignee) {
        if (assignee instanceof String) {
            String assigneeStr = (String) assignee;
            if (NodeRef.isNodeRef(assigneeStr)) {
                return new NodeRef(assigneeStr);
            }
            return authorityService.getAuthorityNodeRef(assigneeStr);
        }
        return JavaScriptImplUtils.getNodeRef(assignee);
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
