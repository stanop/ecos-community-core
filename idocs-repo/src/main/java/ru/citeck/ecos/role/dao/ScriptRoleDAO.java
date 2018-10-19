package ru.citeck.ecos.role.dao;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
@Component
public class ScriptRoleDAO implements RoleDAO {

    private static final Log logger = LogFactory.getLog(ScriptRoleDAO.class);

    private ScriptService scriptService;
    private AuthorityService authorityService;
    private NodeService nodeService;

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
            return JavaScriptImplUtils.getAuthoritiesSet(result, authorityService);
        } catch (Exception e) {
            logger.warn("Script role evaluation failed", e);
        }

        return Collections.emptySet();
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.authorityService = serviceRegistry.getAuthorityService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Autowired
    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }
}
