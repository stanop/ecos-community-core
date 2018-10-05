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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<NodeRef> types;
	private List<NodeRef> kinds;

	@Override
	public void init() {
		this.contentService = serviceRegistry.getContentService();
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

		List<NodeRef> neededDocuments = new ArrayList<>();

		if (types != null && types.size() > 0) {
			types.forEach(type -> {
				if (kinds != null && kinds.size() > 0) {
					kinds.forEach(kind -> {
						childAssocs.forEach(childAssoc -> {
							NodeRef childNodeRef = childAssoc.getChildRef();

							NodeRef currentType = getNodeRef(nodeService.getProperty(childNodeRef, ClassificationModel.PROP_DOCUMENT_TYPE));
							NodeRef currentKind = getNodeRef(nodeService.getProperty(childNodeRef, ClassificationModel.PROP_DOCUMENT_KIND));

							ContentData contentData = (ContentData) nodeService.getProperty(childNodeRef, ContentModel.PROP_CONTENT);
							String mimeType = contentData.getMimetype();

							if (MimetypeMap.MIMETYPE_PDF.equals(mimeType) && currentType.equals(type) && currentKind.equals(kind)) {
								neededDocuments.add(childNodeRef);
							}
						});
					});
				}
			});
		}

		if (neededDocuments.size() == 0) {
			return null;
		}

		List<DataBundle> neededDocumentsDataBundles = new ArrayList<>();

		neededDocuments.forEach(neededDocument -> {
			ContentReader reader = contentService.getReader(neededDocument, contentPropertyName);
			DataBundle dataBundle = helper.getDataBundle(reader, model);
			neededDocumentsDataBundles.add(dataBundle);
		});

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
		return (null != arg) ? (NodeRef)arg : null;
	}

	/**
	 * Set nodeRef.
	 * It can be specified as expression in the supported format.
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

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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

}
