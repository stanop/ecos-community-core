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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.processor.FilteredFormProcessor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.counter.EnumerationException;
import ru.citeck.ecos.counter.EnumerationService;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;
import ru.citeck.ecos.service.CiteckServices;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.*;

public abstract class AbstractNumberFormProcessor<ItemType> extends FilteredFormProcessor<ItemType,String>
{
	protected DictionaryService dictionaryService;
	protected NamespaceService namespaceService;
	protected EnumerationService enumerationService;
	protected NodeInfoFactory nodeInfoFactory;
	protected String templateNameField;
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.dictionaryService = serviceRegistry.getDictionaryService();
		this.namespaceService = serviceRegistry.getNamespaceService();
		this.enumerationService = (EnumerationService) serviceRegistry.getService(CiteckServices.ENUMERATION_SERVICE);
	}
	
	public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
		this.nodeInfoFactory = nodeInfoFactory;
	}
	
	public void setTemplateNameField(String templateNameField) {
		this.templateNameField = templateNameField;
	}
	
	@Override
	protected String internalPersist(ItemType item, FormData data) {
		
		// get template
		String templateName = this.getTemplateName(data);
		NodeRef template = enumerationService.getTemplate(templateName);
		if(template == null) {
			throw new IllegalArgumentException("Can not find template " + templateName);
		}
		
		// construct nodeInfo for enumerationService
		NodeInfo nodeInfo = createNodeInfo(item);
		
		// fill nodeInfo with form data
		Set<String> fieldNames = data.getFieldNames();
		for(String fieldName : fieldNames) {
			FieldData fieldData = data.getFieldData(fieldName);
			Object fieldValue = fieldData.getValue();
			if(fieldName.startsWith(PROP_DATA_PREFIX)) {
				processPropertyField(nodeInfo, fieldName, fieldValue);
			} else if(fieldName.startsWith(ASSOC_DATA_PREFIX)) {
				processAssociationField(nodeInfo, fieldName, fieldValue);
			} else {
				getLogger().warn("Ignoring field, that is neither property, nor association: " + fieldName);
			}
		}
		
		// generate number:
		try {
			return enumerationService.getNumber(template, nodeInfo);
		} catch (EnumerationException e) {
			throw new FormException(e.getMessage(), e);
		}
	}

	/**
	 * Create NodeInfo from item for enumeration service.
	 * 
	 * @param item
	 * @return
	 */
	protected abstract NodeInfo createNodeInfo(ItemType item);

	protected void processPropertyField(NodeInfo nodeInfo, String fieldName,
			Object fieldValue) 
	{
		String prefixedQName = fieldName.substring(PROP_DATA_PREFIX.length()).replaceFirst("_", ":");
		QName propertyName = QName.createQName(prefixedQName, namespaceService);
		PropertyDefinition propertyDefinition = dictionaryService.getProperty(propertyName);
		if(propertyDefinition != null) {
			nodeInfo.setProperty(propertyName, (Serializable) fieldValue);
		} else {
			getLogger().warn("Property " + prefixedQName + " (field " + fieldName + ") does not exist");
		}
	}
	
	protected void processAssociationField(NodeInfo nodeInfo, String fieldName,
			Object fieldValue) 
	{
		int endIndex = 0;
		boolean added;
		if(fieldName.endsWith(ASSOC_DATA_ADDED_SUFFIX)) {
			added = true;
			endIndex = fieldName.length() - ASSOC_DATA_ADDED_SUFFIX.length();
		} else if(fieldName.endsWith(ASSOC_DATA_REMOVED_SUFFIX)) {
			added = false;
			endIndex = fieldName.length() - ASSOC_DATA_REMOVED_SUFFIX.length();
		} else {
			getLogger().warn("Ignoring association field, that is not ended with " + 
				ASSOC_DATA_ADDED_SUFFIX + " or " + ASSOC_DATA_REMOVED_SUFFIX + ": " + fieldName);
			return;
		}
		String prefixedQName = fieldName.substring(ASSOC_DATA_PREFIX.length(), endIndex).replaceFirst("_", ":");
		QName associationName = QName.createQName(prefixedQName, namespaceService);
		AssociationDefinition associationDefinition = dictionaryService.getAssociation(associationName);
		if(associationDefinition == null) {
			getLogger().warn("Association" + prefixedQName + " (field " + fieldName + ") does not exist");
			return;
		}
		
		String valueString = (String) fieldValue;
		if(valueString == null || valueString.isEmpty()) {
			return;
		}
		
		String[] values = valueString.split("[,]");
		List<NodeRef> nodes = new ArrayList<NodeRef>(values.length);
		for(String value : values) {
			if(NodeRef.isNodeRef(value)) {
				nodes.add(new NodeRef(value));
			}
		}
		
		if(associationDefinition.isChild()) {
			if(added) {
				for(NodeRef node : nodes) {
					nodeInfo.addChild(node, associationName);
				}
			} else {
				for(NodeRef node : nodes) {
					nodeInfo.removeChild(node, associationName);
				}
			}
		} else {
			if(added) {
				for(NodeRef node : nodes) {
					nodeInfo.createTargetAssociation(node, associationName);
				}
			} else {
				for(NodeRef node : nodes) {
					nodeInfo.removeTargetAssociation(node, associationName);
				}
			}
		}
	}
	
	protected String getTemplateName(FormData data) {
		FieldData fieldData = data.getFieldData(templateNameField);
		data.removeFieldData(templateNameField);
		return (String) fieldData.getValue();
	}
	
	/*------------------------------------------------------------*/
	/* this form processor is used for persisting only            */
	/* the following stub methods are intended for generation     */
	/*------------------------------------------------------------*/
	
	@Override
	protected List<String> getDefaultIgnoredFields() {
		return null;
	}

	@Override
	protected Object makeItemData(ItemType item) {
		return null;
	}

}
