package ru.citeck.ecos.records.source.alf;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * @author Roman Makarskiy
 */
@Log4j
@Component
public class AlfNodeContentFileHelper {

    private static final String MODEL_MIME_TYPE = "mimetype";
    private static final String MODEL_FILE_NAME = "filename";
    private static final String MODEL_ENCODING = "encoding";
    private static final String MODEL_CONTENT = "content";
    private static final String MODEL_DATA = "data";
    private static final String MODEL_NODE_REF = "nodeRef";

    private static final String MODEL_FILE_TYPE = "fileType";
    private static final String FILE_TYPE_DELIMITER = "/";
    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";

    //TODO: refactor?
    private static final String TYPE_KIND_NAMESPACE = "http://www.citeck.ru/model/content/classification/tk/1.0";
    private static final QName PROP_DOCUMENT_TYPE = QName.createQName(TYPE_KIND_NAMESPACE, "type");
    private static final QName PROP_DOCUMENT_KIND = QName.createQName(TYPE_KIND_NAMESPACE, "kind");

    private final NodeService nodeService;
    private final ContentService contentService;
    private final MimetypeService mimetypeService;
    private final DictionaryService dictionaryService;

    @Autowired
    public AlfNodeContentFileHelper(NodeService nodeService, ContentService contentService,
                                    MimetypeService mimetypeService, DictionaryService dictionaryService) {
        this.nodeService = nodeService;
        this.contentService = contentService;
        this.mimetypeService = mimetypeService;
        this.dictionaryService = dictionaryService;
    }

    void processPropFileContent(NodeRef nodeRef, QName prop, JsonNode jsonNode) {
        ContentWriter writer = contentService.getWriter(nodeRef, prop, true);

        if (jsonNode.isTextual()) {
            writer.putContent(jsonNode.asText());
        } else if (jsonNode.isObject()) {
            JsonNode mimetypeProp = jsonNode.path(MODEL_MIME_TYPE);
            String mimetype = mimetypeProp.isTextual() ? mimetypeProp.asText() : MimetypeMap.MIMETYPE_BINARY;
            if (MimetypeMap.MIMETYPE_BINARY.equals(mimetype)) {
                JsonNode filename = jsonNode.path(MODEL_FILE_NAME);
                if (filename.isTextual()) {
                    mimetype = mimetypeService.guessMimetype(filename.asText());
                }
            }
            writer.setMimetype(mimetype);

            JsonNode encoding = jsonNode.path(MODEL_ENCODING);
            if (encoding.isTextual()) {
                writer.setEncoding(encoding.asText());
            } else {
                writer.setEncoding(StandardCharsets.UTF_8.name());
            }
            JsonNode content = jsonNode.path(MODEL_CONTENT);
            if (content.isTextual()) {
                writer.putContent(content.asText());
            }
        } else if (isFileFromEformFormat(jsonNode)) {
            if (jsonNode.size() > 1) {
                log.warn(String.format("Only one file can be written to the content property <%s>. " +
                        "Current files count: <%s>. The first file will be written.", prop, jsonNode.size()));
            }
            saveFileToContentPropFromEform(jsonNode.get(0), prop, nodeRef);
        }
    }

    void processChildAssocFilesContent(QName assoc, JsonNode jsonNodes, NodeRef nodeRef) {
        ChildAssociationDefinition assocDef = (ChildAssociationDefinition) dictionaryService.getAssociation(assoc);
        QName assocName = assocDef.getName();
        QName targetName = assocDef.getTargetClass().getName();

        jsonNodes.forEach(jsonNode -> {
            NodeRef createdNode = nodeService.createNode(nodeRef,
                    assocName,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                    targetName).getChildRef();

            if (log.isDebugEnabled()) {
                log.debug(String.format("Create child node. Parent <%s>, type <%s>, assocType <%s>, nodeRef <%s>",
                        nodeRef.toString(), targetName, assocName, createdNode.toString()));
            }

            saveFileToContentPropFromEform(jsonNode, ContentModel.PROP_CONTENT, createdNode);
            processTypeKind(jsonNode, createdNode);
        });
    }

    private void processTypeKind(JsonNode jsonNode, NodeRef nodeRef) {
        JsonNode fileTypeNode = jsonNode.get(MODEL_FILE_TYPE);
        if (fileTypeNode == null) {
            return;
        }

        String fileType = fileTypeNode.asText();
        if (StringUtils.isBlank(fileType)) {
            return;
        }

        String rawType = StringUtils.substringBefore(fileType, FILE_TYPE_DELIMITER);
        String rawKind = StringUtils.substringAfter(fileType, FILE_TYPE_DELIMITER);

        if (StringUtils.isBlank(rawType)) {
            return;
        }
        NodeRef type = new NodeRef(WORKSPACE_PREFIX + rawType);
        nodeService.setProperty(nodeRef, PROP_DOCUMENT_TYPE, type);

        if (StringUtils.isBlank(rawKind)) {
            return;
        }

        NodeRef kind = new NodeRef(WORKSPACE_PREFIX + rawKind);
        nodeService.setProperty(nodeRef, PROP_DOCUMENT_KIND, kind);
    }

    private void saveFileToContentPropFromEform(JsonNode tempJsonNode, QName propName, NodeRef node) {
        String tempRefStr = tempJsonNode.get(MODEL_DATA).get(MODEL_NODE_REF).asText();
        if (StringUtils.isBlank(tempRefStr) || !NodeRef.isNodeRef(tempRefStr)) {
            throw new AlfrescoRuntimeException("NodeRef of content file incorrect");
        }

        NodeRef tempFile = new NodeRef(tempRefStr);
        ContentReader reader = contentService.getReader(tempFile, propName);
        ContentWriter writer = contentService.getWriter(node, propName, true);
        writer.setEncoding(reader.getEncoding());
        writer.setMimetype(reader.getMimetype());
        writer.putContent(reader);

        Serializable fileName = nodeService.getProperty(tempFile, ContentModel.PROP_NAME);
        nodeService.setProperty(node, ContentModel.PROP_NAME, fileName);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Copy content from <%s> to <%s>, fileName: <%s>", tempFile.toString(),
                    node.toString(), fileName));
        }

        nodeService.deleteNode(tempFile);
    }

    boolean isFileFromEformFormat(JsonNode jsonNode) {
        if (!jsonNode.isArray() || jsonNode.size() == 0) {
            return false;
        }

        for (JsonNode node : jsonNode) {
            JsonNode data = node.get(MODEL_DATA);
            if (data == null) {
                return false;
            }

            JsonNode nodeRefObj = data.get(MODEL_NODE_REF);
            if (nodeRefObj == null) {
                return false;
            }

            String nodeRef = nodeRefObj.asText();
            if (!NodeRef.isNodeRef(nodeRef)) {
                return false;
            }
        }

        return true;
    }

}
