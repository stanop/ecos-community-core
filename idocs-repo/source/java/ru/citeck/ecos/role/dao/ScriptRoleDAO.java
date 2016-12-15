package ru.citeck.ecos.role.dao;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ICaseRoleModel;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class ScriptRoleDAO extends AbstractRoleDAO {

    private static final Log LOGGER = LogFactory.getLog(ScriptRoleDAO.class);

    private ScriptService scriptService;
    private AuthorityService authorityService;

    @Override
    public QName getRoleType() {
        return ICaseRoleModel.TYPE_SCRIPT_ROLE;
    }

    @Override
    public Set<NodeRef> getAssignees(NodeRef caseRef, NodeRef roleRef) {

        Map<String, Object> model = new HashMap<>();
        model.put("document", caseRef);
        model.put("role", roleRef);

        String script = (String) nodeService.getProperty(roleRef, ICaseRoleModel.PROP_SCRIPT);

        if (script == null || StringUtils.isBlank(script)) {
            return Collections.emptySet();
        }

        try {
            Object result = scriptService.executeScriptString(script, model);
            return fillRoles(new HashSet<NodeRef>(), result);
        } catch (Exception e) {
            LOGGER.warn(e);
        }

        return Collections.emptySet();
    }

    private Set<NodeRef> fillRoles(Set<NodeRef> roles, Object value) {
        if (value == null) {
            return roles;
        } else if (value instanceof NodeRef) {
            roles.add((NodeRef) value);
        } else if (value instanceof String) {
            NodeRef nodeRef = authorityService.getAuthorityNodeRef((String) value);
            if (nodeRef != null) {
                roles.add(nodeRef);
            } else {
                LOGGER.warn("Authority with name '" + value + "' not found!");
            }
        } else if (value instanceof Collection) {
            for (Object item : (Collection) value) {
                fillRoles(roles, item);
            }
        }
        return roles;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
