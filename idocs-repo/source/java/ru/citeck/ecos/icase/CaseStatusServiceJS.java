package ru.citeck.ecos.icase;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

/**
 * @author Roman Makarskiy
 */
public class CaseStatusServiceJS extends AlfrescoScopableProcessorExtension {

    private CaseStatusService caseStatusService;

    public void setStatus(Object documentRef, Object caseStatusRef) {
        NodeRef statusRef;

        if (caseStatusRef instanceof String && !NodeRef.isNodeRef(caseStatusRef.toString())) {
            statusRef = caseStatusService.getStatusByName(caseStatusRef.toString());
        } else {
            statusRef = JavaScriptImplUtils.getNodeRef(caseStatusRef);
        }

        NodeRef docRef = JavaScriptImplUtils.getNodeRef(documentRef);
        caseStatusService.setStatus(docRef, statusRef);
    }

    public ScriptNode getStatusByName(String statusName) {
        NodeRef caseStatusRef = caseStatusService.getStatusByName(statusName);
        return JavaScriptImplUtils.wrapNode(caseStatusRef, this);
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }
}
