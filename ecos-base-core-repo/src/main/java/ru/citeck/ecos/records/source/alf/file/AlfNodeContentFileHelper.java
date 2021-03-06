package ru.citeck.ecos.records.source.alf.file;

import lombok.extern.log4j.Log4j;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commons.data.DataValue;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ru.citeck.ecos.records.source.alf.file.FileRepresentation.*;

/**
 * @author Roman Makarskiy
 */
@Log4j
@Component
public class AlfNodeContentFileHelper {

    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";

    private static NodeRef attachmentRoot = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "attachments-root");

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

    public void processPropFileContent(NodeRef nodeRef, QName prop, DataValue jsonNode) {
        ContentWriter writer = contentService.getWriter(nodeRef, prop, true);

        if (jsonNode.isObject() && jsonNode.get("/data/nodeRef").isTextual()) {
            DataValue asList = Json.getMapper().convert(Collections.singletonList(jsonNode), DataValue.class);
            if (asList == null) {
                log.error("Json node is null after conversion. Node: " + jsonNode);
                return;
            }
            jsonNode = asList;
        }

        if (jsonNode.isTextual()) {
            writer.putContent(jsonNode.asText());
        } else if (jsonNode.isObject()) {
            DataValue mimetypeProp = jsonNode.get(MODEL_MIME_TYPE);
            String mimetype = mimetypeProp.isTextual() ? mimetypeProp.asText() : MimetypeMap.MIMETYPE_BINARY;
            if (MimetypeMap.MIMETYPE_BINARY.equals(mimetype)) {
                DataValue filename = jsonNode.get(MODEL_FILE_NAME);
                if (filename.isTextual()) {
                    mimetype = mimetypeService.guessMimetype(filename.asText());
                }
            }
            writer.setMimetype(mimetype);

            DataValue encoding = jsonNode.get(MODEL_ENCODING);
            if (encoding.isTextual()) {
                writer.setEncoding(encoding.asText());
            } else {
                writer.setEncoding(StandardCharsets.UTF_8.name());
            }
            DataValue content = jsonNode.get(MODEL_CONTENT);
            if (content.isTextual()) {
                writer.putContent(content.asText());
            }
        } else if (isFileFromEformFormat(jsonNode)) {
            if (jsonNode.size() == 0) {
                nodeService.removeProperty(nodeRef, prop);
            } else {
                if (jsonNode.size() > 1) {
                    log.warn(String.format("Only one file can be written to the content property <%s>. " +
                            "Current files count: <%s>. The first file will be written.", prop, jsonNode.size()));
                }

                saveFileToContentPropFromEform(jsonNode.get(0), prop, nodeRef);
            }
        }
    }

    public boolean isFileFromEformFormat(DataValue jsonNode) {
        if (!jsonNode.isArray()) {
            return false;
        }

        for (DataValue node : jsonNode) {
            DataValue data = node.get(MODEL_DATA);
            if (data.isNull()) {
                return false;
            }

            DataValue nodeRefObj = data.get(MODEL_NODE_REF);
            if (nodeRefObj.isNull()) {
                return false;
            }

            String nodeRef = nodeRefObj.asText();
            if (!NodeRef.isNodeRef(nodeRef)) {
                return false;
            }
        }
        return true;
    }

    public void processChildAssocFilesContent(QName assoc, DataValue jsonNodes, NodeRef nodeRef) {
        processAssocFilesContent(assoc, jsonNodes, nodeRef, true);
    }

    public void processAssocFilesContent(QName assoc, DataValue jsonNodes, NodeRef nodeRef, Boolean isChild) {
        AssociationDefinition assocDef = dictionaryService.getAssociation(assoc);
        QName assocName = assocDef.getName();
        QName targetName = assocDef.getTargetClass().getName();

        List<NodeRef> currentFiles = isChild
                ? RepoUtils.getChildrenByAssoc(nodeRef, assocName, nodeService)
                : RepoUtils.getTargetAssoc(nodeRef, assocName, nodeService);
        Set<NodeRef> inboundMutatedRefs = new HashSet<>();

        for (DataValue jsonNode : jsonNodes) {
            String tempRefStr = jsonNode.get(MODEL_DATA).get(MODEL_NODE_REF).asText();
            NodeRef fileRef = new NodeRef(tempRefStr);
            inboundMutatedRefs.add(fileRef);

            if (currentFiles.contains(fileRef)) {
                processTypeKind(jsonNode, fileRef);
                continue;
            }

            NodeRef createdNode;

            if (!isChild) {
                createdNode = nodeService.createNode(attachmentRoot,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                        targetName).getChildRef();

                RepoUtils.createAssociation(nodeRef, createdNode, assocName, true, nodeService);
            } else {
                createdNode = nodeService.createNode(nodeRef,
                        assocName,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                        targetName).getChildRef();
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Create file node. Source node <%s>, type <%s>, assocType <%s>, nodeRef <%s>",
                        nodeRef.toString(), targetName, assocName, createdNode.toString()));
            }

            saveFileToContentPropFromEform(jsonNode, ContentModel.PROP_CONTENT, createdNode);
            processTypeKind(jsonNode, createdNode);
        }

        if (isChild) {
            processDeletion(currentFiles, inboundMutatedRefs);
        } else {
            currentFiles.stream()
                    .filter(ref -> !inboundMutatedRefs.contains(ref))
                    .forEach(ref -> RepoUtils.removeAssociation(nodeRef, ref, assocName, true, nodeService));
        }
    }

    private void saveFileToContentPropFromEform(DataValue tempJsonNode, QName propName, NodeRef node) {
        String tempRefStr = tempJsonNode.get(MODEL_DATA).get(MODEL_NODE_REF).asText();
        if (StringUtils.isBlank(tempRefStr) || !NodeRef.isNodeRef(tempRefStr)) {
            throw new AlfrescoRuntimeException("NodeRef of content file incorrect");
        }

        NodeRef tempFile = new NodeRef(tempRefStr);
        ContentReader reader = contentService.getReader(tempFile, ContentModel.PROP_CONTENT);
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

    private void processTypeKind(DataValue jsonNode, NodeRef nodeRef) {
        DataValue fileTypeNode = jsonNode.get(MODEL_FILE_TYPE);
        if (fileTypeNode.isNull()) {
            return;
        }

        String fileType = fileTypeNode.asText();
        if (StringUtils.isBlank(fileType)) {
            nodeService.removeProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_TYPE);
            nodeService.removeProperty(nodeRef, ClassificationModel.PROP_DOCUMENT_KIND);
            return;
        }

        String rawType = StringUtils.substringBefore(fileType, FILE_TYPE_DELIMITER);
        String rawKind = StringUtils.substringAfter(fileType, FILE_TYPE_DELIMITER);

        updateClassificationIfRequired(nodeRef, ClassificationModel.PROP_DOCUMENT_TYPE, rawType);
        updateClassificationIfRequired(nodeRef, ClassificationModel.PROP_DOCUMENT_KIND, rawKind);
    }

    private void updateClassificationIfRequired(NodeRef nodeRef, QName classificationProperty, String rawValue) {
        if (StringUtils.isBlank(rawValue)) {
            nodeService.removeProperty(nodeRef, classificationProperty);
            return;
        }
        NodeRef classification = new NodeRef(WORKSPACE_PREFIX + rawValue);
        Serializable currentClassification = nodeService.getProperty(nodeRef, classificationProperty);
        if (!Objects.equals(currentClassification, classification)) {
            nodeService.setProperty(nodeRef, classificationProperty, classification);
        }
    }

    private void processDeletion(List<NodeRef> currentChilds, Set<NodeRef> inboundMutatedRefs) {
        currentChilds.stream()
                .filter(ref -> !inboundMutatedRefs.contains(ref))
                .forEach(nodeService::deleteNode);
    }

}
