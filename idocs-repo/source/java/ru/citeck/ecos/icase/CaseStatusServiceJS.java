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

    public void setStatus(Object caseRef, Object caseStatus) {
        NodeRef caseStatusRef;

        if (caseStatus instanceof String && !NodeRef.isNodeRef(caseStatus.toString())) {
            caseStatusRef = caseStatusService.getStatusByName(caseStatus.toString());
        } else {
            caseStatusRef = JavaScriptImplUtils.getNodeRef(caseStatus);
        }

        NodeRef docRef = JavaScriptImplUtils.getNodeRef(caseRef);
        caseStatusService.setStatus(docRef, caseStatusRef);
    }

    public ScriptNode getStatusByName(String statusName) {
        NodeRef caseStatusRef = caseStatusService.getStatusByName(statusName);
        return JavaScriptImplUtils.wrapNode(caseStatusRef, this);
    }

    public String getStatus(Object document) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        return caseStatusService.getStatus(docRef);
    }

    public ScriptNode getStatusNode(Object document) {
        NodeRef docRef = JavaScriptImplUtils.getNodeRef(document);
        NodeRef statusRef = caseStatusService.getStatusRef(docRef);
        return JavaScriptImplUtils.wrapNode(statusRef, this);
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }
}
