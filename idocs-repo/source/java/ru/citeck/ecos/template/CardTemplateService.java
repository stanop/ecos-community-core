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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.CDLModel;
import ru.citeck.ecos.model.DmsModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Alexander Nemerov
 * @date: 04.12.13
 */
public class CardTemplateService {

	private SearchService searchService;
	private DictionaryService dictionaryService;
	private NodeService nodeService;


	public List<NodeRef> getTemplatesForType(QName documentType, String templateType) {
		String query = MessageFormat.format("TYPE:\"{0}\" AND ISNOTNULL:\"{1}\" AND @{1}:\"{2}\" AND @{3}:\"{4}\"",
				DmsModel.TYPE_CARD_TEMPLATE,
				DmsModel.PROP_CARD_TYPE, documentType,
				DmsModel.PROP_TEMPLATE_TYPE, templateType);
		ResultSet nodes = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
		List<NodeRef> results = new ArrayList<NodeRef>(nodes.length());
		if (nodes.length() == 0) {
			QName parent = dictionaryService.getType(documentType).getParentName();
			if(parent != null) {
				return getTemplatesForType(parent, templateType);
			}
		}
		for (ResultSetRow node : nodes) {
			results.add(node.getNodeRef());
		}
		return results;
	}

	public List<NodeRef> getTemplates(QName documentType) {
		List<NodeRef> nodes = getTemplateTypes();
		List<NodeRef> results = new ArrayList<NodeRef>();
		for (NodeRef nodeRef : nodes) {
			List<NodeRef> templatesForType = getTemplatesForType(documentType,
					(String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
			if (templatesForType != null && templatesForType.size() > 0) {
				results.add(templatesForType.get(0));
			}
		}
		return results;
	}

	public List<NodeRef> getTemplateTypes() {
		String query = "TYPE:\"" + CDLModel.TYPE_CARD_TEMPLATE_TYPE + "\"";
		ResultSet templatesResultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
		List<NodeRef> results = new ArrayList<NodeRef>();
		for (ResultSetRow row : templatesResultSet) {
			results.add(row.getNodeRef());
		}
		return results;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
