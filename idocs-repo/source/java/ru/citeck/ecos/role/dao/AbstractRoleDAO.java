package ru.citeck.ecos.role.dao;

import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.role.CaseRoleService;

/**
 * @author Pavel Simonov
 */
public abstract class AbstractRoleDAO implements RoleDAO {

    private CaseRoleService caseRoleService;
    protected NodeService nodeService;

    public void init() {
        caseRoleService.register(this);
    }

    public void setCaseRoleService(CaseRoleService caseRoleService) {
        this.caseRoleService = caseRoleService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
