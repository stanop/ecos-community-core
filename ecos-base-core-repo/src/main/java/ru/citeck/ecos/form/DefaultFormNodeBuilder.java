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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

public class DefaultFormNodeBuilder extends AbstractFormNodeBuilder {

	private static final String NAME_PROP_DATA = "prop_cm_name";

	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private SearchService searchService;

	// taken from original TypeFormProcessor
	@Override
	public NodeRef createNode(TypeDefinition typeDef, FormData data) {
		NodeRef nodeRef = null;

		if (data != null)
		{
			// firstly, ensure we have a destination to create the node in
			NodeRef parentRef = null;
			FieldData destination = data.getFieldData(DESTINATION);
			if (destination == null) 
			{ 
				throw new FormException("Failed to persist form for '"
						+ typeDef.getName().toPrefixString(this.namespaceService) + 
						"' as '" + DESTINATION + "' data was not provided."); 
			}

			// create the parent NodeRef
			parentRef = getDestinationNodeRef(destination);

			// remove the destination data to avoid warning during persistence,
			// this can
			// always be retrieved by looking up the created node's parent
			data.removeFieldData(DESTINATION);

			QName assocType = ContentModel.ASSOC_CONTAINS;
			FieldData assocTypeParam = data.getFieldData(ASSOC_TYPE);
			if (assocTypeParam != null)
			{
				String assocTypeStr = (String) assocTypeParam.getValue();
				if (!assocTypeStr.isEmpty()) {
					QName at = QName.createQName(assocTypeStr,
							namespaceService);
					AssociationDefinition assocDef = dictionaryService.getAssociation(at);
					if (assocDef != null)
						assocType = at;
					data.removeFieldData(ASSOC_TYPE);
				}
			}

			// if a name property is present in the form data use it as the node
			// name,
			// otherwise generate a guid
			String nodeName = null;
			FieldData nameData = data.getFieldData(NAME_PROP_DATA);
			if (nameData != null)
			{
				nodeName = (String) nameData.getValue();

				// remove the name data otherwise 'rename' gets called in
				// persistNode
				data.removeFieldData(NAME_PROP_DATA);
			}
			if (nodeName == null || nodeName.length() == 0)
			{
				nodeName = GUID.generate();
			}

			// create the node
			Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>(1);
			nodeProps.put(ContentModel.PROP_NAME, nodeName);
			nodeRef = this.nodeService.createNode(
					parentRef,
					assocType,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
							QName.createValidLocalName(nodeName)),
							typeDef.getName(),
							nodeProps).getChildRef();
		}

		return nodeRef;
	}

	private NodeRef getDestinationNodeRef(FieldData destination) {
		NodeRef parentRef;
		String destinationString = (String) destination.getValue();
		if(NodeRef.isNodeRef(destinationString)) {
			parentRef = new NodeRef((String) destination.getValue());
		} else {
			ResultSet results = null;
			try {
				results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, destinationString);
				if(results.length() == 0) {
					throw new FormException("Can not find destination: " + destinationString); 
				}
				parentRef = results.getNodeRef(0);
			} finally {
				if(results != null) {
					results.close();
				}
			}
		}
		return parentRef;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
}
