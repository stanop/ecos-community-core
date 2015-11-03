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

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import ru.citeck.ecos.form.FormConstants;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mstrizhov on 21.08.2014.
 */
public class VersionsCommentFormFilter extends AbstractFilter<Object, NodeRef> {
    private static Log logger = LogFactory.getLog(VersionsCommentFormFilter.class);
    
    private static final String CREATE_VERSION_FLAG = "alf_create_version";
    private static final String DEFAULT_VERSION_DESCRIPTION = "version.properties.changed";
    
    private NodeService nodeService;
    private VersionService versionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    public void beforeGenerate(Object item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
        // do nothing
    }

    @Override
    public void afterGenerate(Object item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
        // do nothing
    }

    @Override
    public void beforePersist(Object item, FormData data) {
        // do nothing
    }

    @Override
    public void afterPersist(Object item, FormData data, final NodeRef persistedObject) {
        if(!data.hasFieldData(CREATE_VERSION_FLAG) ||
                data.getFieldData(CREATE_VERSION_FLAG).getValue() == null ||
                !data.getFieldData(CREATE_VERSION_FLAG).getValue().equals("true") ||
                !nodeService.exists(persistedObject)) {
            return;
        }
        Object historyMessageId = data.hasFieldData(FormConstants.HISTORY_DESCRIPTION_FIELD) ? 
                data.getFieldData(FormConstants.HISTORY_DESCRIPTION_FIELD).getValue() : 
                DEFAULT_VERSION_DESCRIPTION;
        final String historyMessage = I18NUtil.getMessage((String) historyMessageId);

        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            public void beforeCommit(boolean readOnly) {
                if (readOnly) {
                    logger.warn("Tried to persist form in read-only transaction");
                    return;
                }

                // ensure this is the last listener executed
                AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                    public void beforeCommit(boolean readOnly) {
                        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(3);
                        versionProperties.put(VersionModel.PROP_DESCRIPTION, historyMessage);
                        RepoUtils.createVersion(persistedObject, versionProperties, nodeService, versionService);
                    }
                });

            }
        });
    }
}
