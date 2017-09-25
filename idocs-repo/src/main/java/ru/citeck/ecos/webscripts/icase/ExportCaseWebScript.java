package ru.citeck.ecos.webscripts.icase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.cmmn.service.CaseExportService;
import org.alfresco.repo.content.MimetypeMap;
import ru.citeck.ecos.server.utils.Utils;

import java.io.IOException;

/**
 * WebScript for export template for case from the system in CMMN compatible format
 *
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 */
public class ExportCaseWebScript extends AbstractWebScript {
    private static final Logger logger = Logger.getLogger(ExportCaseWebScript.class);

    private CaseExportService caseExportService;

    public void setCaseExportService(CaseExportService caseExportService) {
        this.caseExportService = caseExportService;
    }

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        String nodeRefParam = request.getParameter("nodeRef");
        NodeRef nodeRef = new NodeRef(nodeRefParam);

        byte[] caseTemplateContent = caseExportService.exportCase(nodeRef);
        response.getOutputStream().write(caseTemplateContent);
        response.setContentType(MimetypeMap.MIMETYPE_XML);
        response.setHeader("Content-Disposition",
                Utils.encodeContentDispositionForDownload(request, "exported-case","xml", false));
    }
}
