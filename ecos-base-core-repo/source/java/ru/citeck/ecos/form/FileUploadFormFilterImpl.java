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
package ru.citeck.ecos.form;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import ru.citeck.ecos.utils.RepoUtils;

/**
 * Form Filter that helps to save cm:content.
 * Form should have multipart/form-data enctype to support content transfer.
 * 
 */
public class FileUploadFormFilterImpl extends AbstractFilter<Object, NodeRef> implements FileUploadFormFilter {

	private static final Log logger = LogFactory.getLog(FileUploadFormFilterImpl.class);
	private static final String ERROR_CAN_NOT_GET_CONTENT_SIZE = "error.can.not.get.content.size";
	private static final String ERROR_EXCEEDED_CONTENT_SIZE = "error.exceeded.content.size";
	private static final String MESSAGE_FILE_UPLOADED = "version.file.uploaded";
	private static final String EMPTY_EXTENSION = "";

	private NodeService nodeService;
	private ContentService contentService;
	private MimetypeService mimetypeService;
	private VersionService versionService;
	private CheckOutCheckInService checkOutCheckInService;
	private Map<QName, Integer> sizeLimits = new HashMap<QName, Integer>();

	@Override
	public void beforePersist(Object item, FormData data) {
		FieldData fieldData = data.getFieldData("prop_cm_content");
		if (fieldData == null || !fieldData.isFile()) {
			return;
		}

		InputStream fileStream = fieldData.getInputStream();
		int contentSize = 0;
		try {
			if (fileStream != null)
				contentSize = fileStream.available();
		} catch (IOException e) {
			throw new AlfrescoRuntimeException(ERROR_CAN_NOT_GET_CONTENT_SIZE);
		}

		if (fileStream == null || contentSize == 0) {
			return;
		}

		QName type = null;
		if (item instanceof NodeRef) {
			type = nodeService.getType((NodeRef) item);
		} else if (item instanceof TypeDefinition) {
			type = ((TypeDefinition) item).getName();
		}
		if (type == null) {
			return;
		}

		Integer sizeLimit = sizeLimits.get(type);
		if (sizeLimit == null || sizeLimit.intValue() == 0) {
			return;
		}

		if (contentSize > sizeLimit) {
			String message = I18NUtil.getMessage(ERROR_EXCEEDED_CONTENT_SIZE, contentSize, sizeLimit);
			if (message == null)
				message = ERROR_EXCEEDED_CONTENT_SIZE;
			throw new AlfrescoRuntimeException("\n" + message);
		}
	}

	@Override
	public void afterPersist(Object item, final FormData data, final NodeRef persistedObject) {
		if (!nodeService.exists(persistedObject)) {
			return;
		}
		
		FieldData fieldData = data.getFieldData("prop_cm_content");
		if (fieldData == null || !fieldData.isFile()) {
			return;
		}

		final String fileName = fieldData.getValue().toString();
		if(fileName.isEmpty()) {
			return;
		}

		final InputStream fileStream = fieldData.getInputStream();
		if (fileStream == null) {
			return;
		}

		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			public void beforeCommit(boolean readOnly) {
				if(readOnly) {
					logger.warn("Tried to persist read-only form");
					return;
				}

				// ensure, that it is executed after all behaviours
				AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
					public void beforeCommit(boolean readOnly) {
						setContent(persistedObject, fileStream, fileName);

						String historyDescriptionText = null;
						if (data.hasFieldData(FormConstants.HISTORY_DESCRIPTION_FIELD)) {
							historyDescriptionText = (String) data.getFieldData(FormConstants.HISTORY_DESCRIPTION_FIELD).getValue();
						}

						Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(3);
						versionProperties.put(VersionModel.PROP_DESCRIPTION, I18NUtil.getMessage(historyDescriptionText != null ? historyDescriptionText : MESSAGE_FILE_UPLOADED));
						RepoUtils.setUniqueOriginalName(persistedObject, EMPTY_EXTENSION, nodeService, mimetypeService);
						RepoUtils.createVersion(persistedObject, versionProperties, nodeService, versionService);
					}
				});
			}
		});
	}

	@Override
	public void beforeGenerate(Object item, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
		// do nothing
	}

	@Override
	public void afterGenerate(Object item, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
		// do nothing
	}

	@Override
	public void registerSizeLimits(Map<QName, Integer> sizeLimits) {
		this.sizeLimits.putAll(sizeLimits);
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

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setSizeLimits(Map<QName, Integer> sizeLimits) {
		this.sizeLimits = sizeLimits;
	}

	protected String setContent(final NodeRef nodeRef, InputStream fileStream,
			String fileName) {
		String mimetype = mimetypeService.guessMimetype(fileName);
		ContentWriter writer = null;
		try {
			writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			writer.setEncoding("UTF-8");
			writer.setMimetype(mimetype);
			writer.putContent(fileStream);
		} finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				logger.error("Failed to close file stream on form submit", e);
			}
		}
		return mimetype;
	}

	protected void ensureVersioningEnabled(final NodeRef nodeRef) {
		Map<QName, Serializable> versionProperties = new HashMap<QName, Serializable>();
		versionProperties.put(ContentModel.PROP_AUTO_VERSION, true);
		versionProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
		versionService.ensureVersioningEnabled(nodeRef, versionProperties);
	}

    protected void checkOutCheckInVersion(final NodeRef nodeRef, String message) {
        NodeRef coNodeRef = checkOutCheckInService.checkout(nodeRef);
        checkOutCheckInService.checkin(coNodeRef, Collections.singletonMap(Version.PROP_DESCRIPTION, (Serializable) message));
    }

}
