package ru.citeck.ecos.icase;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

/**
 * @author Roman.Makarskiy on 11/14/2016.
 */
public class CaseStatusServiceJS extends AlfrescoScopableProcessorExtension {

    private CaseStatusService caseStatusService;

    public void setCaseStatus(Object documentRef, Object caseStatusRef) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(documentRef);
        NodeRef statusRef = JavaScriptImplUtils.getNodeRef(caseStatusRef);
        caseStatusService.setCaseStatus(docRef, statusRef);
    }

    public ScriptNode getCaseStatusByName(String statusName) {
        NodeRef caseStatusRef = caseStatusService.getCaseStatusByName(statusName);
        return JavaScriptImplUtils.wrapNode(caseStatusRef, this);
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }
}
