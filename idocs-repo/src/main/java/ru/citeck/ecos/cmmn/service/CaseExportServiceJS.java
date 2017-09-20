package ru.citeck.ecos.cmmn.service;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author deathNC
 */
public class CaseExportServiceJS extends AlfrescoScopableProcessorExtension {

    private CaseExportService caseExportService;

    public void exportToFile(String nodeRef, String fileName) {
        NodeRef ref = new NodeRef(nodeRef);
        caseExportService.exportCaseToFile(ref, fileName);
    }

    public void setCaseExportService(CaseExportService caseExportService) {
        this.caseExportService = caseExportService;
    }
}
