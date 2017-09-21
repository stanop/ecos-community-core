package ru.citeck.ecos.webscripts.icase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import ru.citeck.ecos.cmmn.service.CaseImportService;

import java.io.IOException;

/**
 * @author Maxim Strizhov (maxim.strizhov@citeck.com)
 */
public class ImportCaseWebScript  extends AbstractWebScript {
    private static final Logger logger = Logger.getLogger(ExportCaseWebScript.class);

    private CaseImportService caseImportService;

    public void setCaseImportService(CaseImportService caseImportService) {
        this.caseImportService = caseImportService;
    }

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        String nodeRefParam = request.getParameter("destination");
        NodeRef nodeRef = new NodeRef(nodeRefParam);
        FormData formData = (FormData) request.parseContent();
        if (formData == null || !formData.getIsMultiPart())
        {
            throw new WebScriptException(400, "Could not read file content from request.");
        }
        for (FormData.FormField field : formData.getFields()) {
            if (field.getIsFile()) {
                logger.info(field.getFilename());
                caseImportService.importCase(field.getInputStream());
            }
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.append("result", "success");
            response.getWriter().write(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
