package ru.citeck.ecos.webscripts.doc;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.server.utils.Utils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File conversion controller. See also webscript
 * export-doc.get.desc.xml in idocs-repo.
 * 
 * @author Andrew Timokhin
 */

public class ExportDocWebScript extends AbstractWebScript {

    private static final Logger logger = Logger.getLogger(ExportDocWebScript.class);

    private static final String GENERATE_CONTENT           = "generate-content";
    private static final String CREATE_MODE                = "create-mode";
    private static final String OUTPUT_ENCODING            = "UTF-8";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    
    private boolean         createMode;
    private ActionService   actionService;
    private NodeService     nodeService;
    private ContentService  contentService;
    private MimetypeService mimetypeService;

    public void setCreateMode(boolean createMode) {
        this.createMode = createMode;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    private ContentReader getContentReader(NodeRef node) {
        ContentReader contentReader = contentService.getReader(node, ContentModel.PROP_CONTENT);
        
        return contentReader.exists()
                ? contentReader
                : null;
    }

    private String getMimetypeByExt(String ext) {
        return ext != null && !ext.isEmpty()
                ? mimetypeService.getMimetype(ext)
                : null;
    }

    private static byte[] getByteArray(ContentReader contentReader) throws IOException {
        if (contentReader == null) {
            return null;
        }

        try (InputStream inputStream = new BufferedInputStream(contentReader.getContentInputStream())) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private static void prepareResponse(WebScriptRequest req, WebScriptResponse res, ContentReader contentReader, String filename, String ext) throws IOException {
        res.getOutputStream().write(getByteArray(contentReader));
        res.setContentType(contentReader.getMimetype());
        res.setHeader(HEADER_CONTENT_DISPOSITION, Utils.encodeContentDispositionForDownload(req, filename, ext, false));
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String nodeRef = req.getParameter("nodeRef"),
                   ext = req.getParameter("ext");

        if (StringUtils.isBlank(nodeRef) || StringUtils.isBlank(ext)) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "The request does not match the template");
        }

        NodeRef node = new NodeRef(nodeRef);

        if (!nodeService.exists(node)) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Node " + nodeRef + " not found");
        }
        
        String mimetypeByExt = getMimetypeByExt(ext);

        if (mimetypeByExt == null) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Mimetype for the extension " + ext + " is not registered");
        }
        
        Action action = actionService.createAction(GENERATE_CONTENT);
        action.setParameterValue(CREATE_MODE, createMode);
        actionService.executeAction(action, node, false, false);

        ContentReader reader = getContentReader(node);

        if (reader == null) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can not create content for a node " + nodeRef);
        }

        if (reader.getMimetype().equalsIgnoreCase(mimetypeByExt)) {
            prepareResponse(req, res, reader, node.getId(), ext);
            return;
        }

        ContentWriter writer = contentService.getTempWriter();

        writer.setEncoding(OUTPUT_ENCODING);
        writer.setMimetype(mimetypeByExt);

        try {
            contentService.transform(reader, writer);
        } catch (Exception exc) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transformation errors from " + reader.getMimetype() + " to " + writer.getMimetype(), exc);
        }

        ContentReader result = writer.getReader();
        
        prepareResponse(req, res, result, node.getId(), ext);
    }
}