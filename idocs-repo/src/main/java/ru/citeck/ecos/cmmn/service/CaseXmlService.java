package ru.citeck.ecos.cmmn.service;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import ru.citeck.ecos.cmmn.model.Case;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.cmmn.service.util.CasePlanModelImport;
import ru.citeck.ecos.cmmn.service.util.CaseRolesImport;
import ru.citeck.ecos.icase.CaseElementService;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;

import java.util.*;


public class CaseXmlService {

    private ServiceRegistry serviceRegistry;

    private NodeService nodeService;
    private AuthorityService authorityService;
    private CaseTemplateRegistry caseTemplateRegistry;

    public void fillCaseFromTemplate(NodeRef targetNodeRef) {
        Definitions definition = caseTemplateRegistry.getDefinitionForCase(targetNodeRef);
        if (definition != null) {
            copyTemplateToCase(definition, targetNodeRef);
        }
    }

    public void copyTemplateToCase(Definitions definition, NodeRef caseRef) {
        Case caseItem = definition.getCase().get(0);
        if (caseRef != null) {
            CaseRolesImport caseRolesImport = new CaseRolesImport(nodeService, authorityService);
            CasePlanModelImport casePlanModelImport = new CasePlanModelImport(serviceRegistry);
            Map<String, NodeRef> rolesRef = caseRolesImport.importRoles(caseRef, caseItem.getCaseRoles());
            casePlanModelImport.importCasePlan(caseRef, caseItem, rolesRef);
        }
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.authorityService = serviceRegistry.getAuthorityService();
        this.caseTemplateRegistry = EcosCoreServices.getCaseTemplateRegistry(serviceRegistry);
    }
}
