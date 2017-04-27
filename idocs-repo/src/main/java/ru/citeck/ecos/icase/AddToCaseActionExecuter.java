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
package ru.citeck.ecos.icase;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.ICaseModel;

/**
 * This action associates case element with case.
 * It can be added to case child folder, so that any documents added to this folder, 
 *  are automatically associated with case.
 * 
 * @author Sergey Tiunov
 *
 */
public class AddToCaseActionExecuter extends ActionExecuterAbstractBase {

	private NodeService nodeService;
	private RuleService ruleService;
	private CaseElementService caseElementService;
	private DictionaryService dictionaryService;
	
	@Override
	protected void executeImpl(Action action, NodeRef documentNode) {
		
		QName documentType = nodeService.getType(documentNode);
		if(!dictionaryService.isSubClass(documentType, ContentModel.TYPE_CONTENT)) {
			return;
		}
		
		NodeRef caseFolder = nodeService.getPrimaryParent(documentNode).getParentRef();
		QName caseFolderType = nodeService.getType(caseFolder);
		if(!dictionaryService.isSubClass(caseFolderType, ContentModel.TYPE_FOLDER)) {
			return;
		}
		
		NodeRef caseNode = nodeService.getPrimaryParent(caseFolder).getParentRef();
		String caseFolderName = (String) nodeService.getProperty(caseFolder, ContentModel.PROP_NAME);
		NodeRef elementConfig = CaseUtils.getConfigByPropertyValue(caseNode, ICaseModel.PROP_FOLDER_NAME, caseFolderName, nodeService, caseElementService);
		if(elementConfig == null) {
//			throw new IllegalArgumentException("Can not find element config for folder " + caseFolderName);
			return;
		}
		
		QName elementType = (QName) nodeService.getProperty(elementConfig, ICaseModel.PROP_ELEMENT_TYPE);
		
		if(!dictionaryService.isSubClass(documentType, elementType)) {
			if(!dictionaryService.isSubClass(elementType, documentType)) {
				throw new IllegalArgumentException("Document " + documentNode + 
						" of type " + documentType + " can not be cast to " + elementType);
			}
			nodeService.setType(documentNode, elementType);
		}

		String elementConfigName = (String) nodeService.getProperty(elementConfig, ContentModel.PROP_NAME);
		caseElementService.addElement(documentNode, caseNode, elementConfigName);
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// no parameters
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public void setCaseElementService(CaseElementService caseElementService) {
		this.caseElementService = caseElementService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

}
