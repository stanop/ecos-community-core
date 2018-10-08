/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.processor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.processor.pdf.PDFMerge;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Output Content is a Data Bundle Generator, that outputs content of specified nodes that associated with specified node.
 * If node does not exist, null is output.
 * If node does not have content, Data Bundle with null input stream is output.
 * 
 * Target nodeRef can be specified as expressions, supported by expression evaluator.
 * 
 * @author Alexander Popov
 *
 */
public class OutputSpecifiedAssociatedDocumentsPdfContent extends AbstractDataBundleLine {

    private ContentService contentService;
    private NodeService nodeService;
    private String nodeRef;
    private QName contentPropertyName = ContentModel.PROP_CONTENT;

    private PDFMerge pdfMerge;
    private QName childAssocQname;
    private List<NodeRef> types = Collections.emptyList();
    private List<NodeRef> kinds = Collections.emptyList();

    @Override
    public void init() {
		this.contentService = serviceRegistry.getContentService();
		this.nodeService = serviceRegistry.getNodeService();
	}

    @Override
    public DataBundle process(DataBundle input) {

        Map<String,Object> model = input.needModel();

        NodeRef document = helper.getExistingNodeRef(evaluateExpression(nodeRef, model));
        if(document == null) {
            return null;
        }

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(document, childAssocQname, RegexQNamePattern.MATCH_ALL);
        if (childAssocs.size() == 0) {
            return null;
        }

        List<Document> allDocuments = new ArrayList<>();

        childAssocs.forEach(childAssoc -> {
            NodeRef childNodeRef = childAssoc.getChildRef();

            Map<QName, Serializable> props = nodeService.getProperties(childNodeRef);
            NodeRef currentType = getNodeRef(props.get(ClassificationModel.PROP_DOCUMENT_TYPE));
            NodeRef currentKind = getNodeRef(props.get(ClassificationModel.PROP_DOCUMENT_KIND));

            ContentData contentData = (ContentData) props.get(contentPropertyName);
            String mimeType = contentData.getMimetype();

            ContentReader contentReader = contentService.getReader(childNodeRef, contentPropertyName);

            Document doc = new Document(currentType, currentKind, mimeType, contentReader);
            allDocuments.add(doc);
        });

        List<DataBundle> neededDocumentsDataBundles = new ArrayList<>();

        types.forEach(type -> {
            kinds.forEach(kind -> {
                allDocuments.forEach(doc -> {
                    if (MimetypeMap.MIMETYPE_PDF.equals(doc.mimeType) && doc.type.equals(type) && doc.kind.equals(kind)) {
                        DataBundle dataBundle = helper.getDataBundle(doc.contentReader, model);
                        neededDocumentsDataBundles.add(dataBundle);
                    }
                });
            });
        });

        if (neededDocumentsDataBundles.size() == 0) {
            return null;
        }

        DataBundle resultDataBundle = pdfMerge.merge(neededDocumentsDataBundles);

        InputStream resultInputStream = resultDataBundle.getInputStream();
        Map<String,Object> resultModel = resultDataBundle.getModel();

        Map<String,Object> modelWithFixedMimeType = new HashMap<>();
        modelWithFixedMimeType.putAll(resultModel);
        modelWithFixedMimeType.put(ProcessorConstants.KEY_MIMETYPE, MimetypeMap.MIMETYPE_PDF);

        DataBundle resultWithFixedMimetype = new DataBundle(resultInputStream, modelWithFixedMimeType);

        return resultWithFixedMimetype;
    }

    private NodeRef getNodeRef(Serializable arg) {
        return (null != arg) ? (NodeRef) arg : null;
    }

    /**
     * Set nodeRef of the parent document.
     * @param nodeRef
     */
    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    /**
     * Set content property name.
     * If it is not set, cm:content is assumed by default.
     *
     * @param contentPropertyName
     */
    public void setContentPropertyName(QName contentPropertyName) {
        this.contentPropertyName = contentPropertyName;
    }

    public void setChildAssocQname(QName childAssocQname) {
        this.childAssocQname = childAssocQname;
    }

    public void setPdfMerge(PDFMerge pdfMerge) {
        this.pdfMerge = pdfMerge;
    }

    public void setTypes(List<NodeRef> types) {
        this.types = types;
    }

    public void setKinds(List<NodeRef> kinds) {
        this.kinds = kinds;
    }

    private class Document {

        private NodeRef type;
        private NodeRef kind;
        private String mimeType;
        private ContentReader contentReader;

        public Document(NodeRef type, NodeRef kind, String mimeType, ContentReader contentReader) {
            this.type = type;
            this.kind = kind;
            this.mimeType = mimeType;
            this.contentReader = contentReader;
        }
    }
}