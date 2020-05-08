package ru.citeck.ecos.cmmn.service;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.model.Case;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.cmmn.service.util.CaseElementImport;
import ru.citeck.ecos.cmmn.service.util.CasePlanModelImport;
import ru.citeck.ecos.cmmn.service.util.CaseRolesImport;
import ru.citeck.ecos.icase.element.CaseElementService;
import ru.citeck.ecos.service.EcosCoreServices;

import java.util.Map;
import java.util.Optional;

@Service("caseXmlService")
@DependsOn("idocs.dictionaryBootstrap")
public class CaseXmlService {

    private ServiceRegistry serviceRegistry;

    private NodeService nodeService;
    private AuthorityService authorityService;
    private CaseTemplateRegistry caseTemplateRegistry;
    private CaseElementService caseElementService;

    @Autowired
    private CMMNUtils utils;

    public void fillCaseFromTemplate(NodeRef targetNodeRef) {
        Optional<Definitions> definition = caseTemplateRegistry.getDefinitionForCase(targetNodeRef);
        definition.ifPresent(definitions -> copyTemplateToCase(definitions, targetNodeRef));
    }

    public void copyTemplateToCase(Definitions definition, NodeRef caseRef) {
        Case caseItem = definition.getCase().get(0);
        if (caseRef != null) {
            CaseRolesImport caseRolesImport = new CaseRolesImport(nodeService, authorityService, utils);
            CasePlanModelImport casePlanModelImport = new CasePlanModelImport(serviceRegistry, utils);
            CaseElementImport caseElementImport = new CaseElementImport(caseElementService);

            Map<String, NodeRef> rolesRef = caseRolesImport.importRoles(caseRef, caseItem.getCaseRoles());
            caseElementImport.importCaseElementTypes(caseRef, caseItem);
            casePlanModelImport.importCasePlan(caseRef, caseItem, rolesRef);
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.authorityService = serviceRegistry.getAuthorityService();
        this.caseTemplateRegistry = EcosCoreServices.getCaseTemplateRegistry(serviceRegistry);
        this.caseElementService = EcosCoreServices.getCaseElementService(serviceRegistry);
    }
}
