package ru.citeck.ecos.eform.webscripts;

import lombok.extern.log4j.Log4j;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import ru.citeck.ecos.model.EcosContentModel;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Log4j
public class FileEformPost extends DeclarativeWebScript {

    private static final String PARAM_NAME = "name";
    private static final String PARAM_FILE = "file";

    private static final NodeRef ROOT_NODE_REF = new NodeRef("workspace://SpacesStore/attachments-root");

    private NodeService nodeService;
    private ContentService contentService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        //TODO: run as system

        String name = req.getParameter(PARAM_NAME);
        if (StringUtils.isBlank(name)) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_NAME + "' should be set");
        }

        File file = new File(req);
        if (file.isInvalid()) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_FILE + "' should be set");
        }

        log.error("Name: " + name);
        log.error("File: " + file);

        Map<QName, Serializable> props = new HashMap<>(2);
        props.put(EcosContentModel.PROP_ID, name);
        props.put(ContentModel.PROP_NAME, file.fileName);

        NodeRef createdTempFile = nodeService.createNode(
                ROOT_NODE_REF,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                ContentModel.TYPE_CONTENT,
                props).getChildRef();

        ContentWriter writer = contentService.getWriter(createdTempFile, ContentModel.PROP_CONTENT, true);
        String encoding = StringUtils.isNoneBlank(file.content.getEncoding()) ? file.content.getEncoding() :
                StandardCharsets.UTF_8.name();
        writer.setEncoding(encoding);

        String mimetype = StringUtils.isNoneBlank(file.content.getMimetype()) ? file.content.getMimetype() :
                file.mimetype;
        writer.setMimetype(mimetype);

        writer.putContent(file.content.getInputStream());

        Map<String, Object> result = new HashMap<>();
        result.put("result", createdTempFile.toString());
        return result;
    }

    private class File {
        String fileName;
        String mimetype;
        Content content;

        File(WebScriptRequest request) {
            FormData formData = (FormData) request.parseContent();
            FormData.FormField[] fields = formData.getFields();
            for (FormData.FormField field : fields) {
                if (PARAM_FILE.equals(field.getName()) && field.getIsFile()) {
                    this.fileName = field.getFilename();
                    this.content = field.getContent();
                    this.mimetype = field.getMimetype();
                    break;
                }
            }
        }

        boolean isInvalid() {
            return StringUtils.isAnyBlank(fileName, mimetype) || content == null;
        }

        @Override
        public String toString() {
            return "File{" +
                    "fileName='" + fileName + '\'' +
                    ", mimetype='" + mimetype + '\'' +
                    ", content=" + content +
                    '}';
        }
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
