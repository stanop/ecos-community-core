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
package ru.citeck.ecos.template;

import org.alfresco.model.RenditionModel;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import ru.citeck.ecos.form.FormConstants;

import java.util.List;
import java.util.Map;

/**
 * This form filter provides generating the cm:content by template, specified
 * in child-association dms:templateAssociation.
 * 
 * @author Sergey Tiunov
 *
 */
public class TemplateFormFilter extends AbstractFilter<Object, NodeRef> {

	private static Log logger = LogFactory.getLog(TemplateFormFilter.class);

	public static final String PROCESS_TEMPLATE_FLAG = "prop_dms_updateContent";
	
    private NodeService nodeService;
    private ActionService actionService;
    private boolean createMode;

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
	public void beforePersist(Object item, FormData data) {
		// do nothing
	}

	@Override
	public void afterPersist(Object item, final FormData data, final NodeRef persistedObject) {
		// do nothing, unless special flag is set
		if(!data.hasFieldData(PROCESS_TEMPLATE_FLAG) ||
				!"true".equals(data.getFieldData(PROCESS_TEMPLATE_FLAG).getValue()) ||
				!nodeService.exists(persistedObject)) {
			return;
		}

		if (logger.isDebugEnabled())
			logger.debug("Generating cm:content by dms:templateAssociation for node=" + persistedObject);

		// generate content by template
		
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			public void beforeCommit(boolean readOnly) {
				if(readOnly) {
					logger.warn("Tried to persist form in read-only transaction");
					return;
				}
				
				// ensure this is the last listener executed
				AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
					public void beforeCommit(boolean readOnly) {
				        Action action = actionService.createAction("generate-content");
				        action.setParameterValue("create-mode", createMode);
				        if(data.hasFieldData(FormConstants.HISTORY_DESCRIPTION_FIELD)) {
    				        String historyDescription = (String) data.getFieldData(FormConstants.HISTORY_DESCRIPTION_FIELD).getValue();
                            if (historyDescription != null) {
                                action.setParameterValue(GenerateContentActionExecuter.PARAM_HISTORY_DESCRIPTION, I18NUtil.getMessage(historyDescription));
                            }
				        }
				        actionService.executeAction(action, persistedObject, false, false);
					}
				});

			}
		});

		// remove all renditions
		// removeRenditions(persistedObject);

	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@SuppressWarnings("unused")
    private void removeRenditions(NodeRef persistedObject) {
		try {
			List<ChildAssociationRef> rnAssocs = nodeService.getChildAssocs(
					persistedObject,
					RenditionModel.ASSOC_RENDITION,
					RegexQNamePattern.MATCH_ALL);
			if (rnAssocs != null) {
				for (ChildAssociationRef rnAssoc : rnAssocs)
					nodeService.removeChildAssociation(rnAssoc);
				if (logger.isDebugEnabled())
					logger.debug("Removed " + rnAssocs.size()
							+ " rendition(s) from node=" + persistedObject);
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("An error occurred, when removing assoc=" + RenditionModel.ASSOC_RENDITION + " from node=" + persistedObject, e);
		}
	}

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setCreateMode(boolean createMode) {
        this.createMode = createMode;
    }

}
