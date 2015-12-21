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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseTemplateModel;
import ru.citeck.ecos.utils.RepoUtils;

public abstract class AbstractCaseElementDAO implements CaseElementDAO {

	protected NodeService nodeService;
	protected DictionaryService dictionaryService;
	protected CopyService copyService;
	protected ActionService actionService;
	protected CaseElementServiceImpl caseElementService;
	protected PolicyComponent policyComponent;

	public void setNodeService(NodeService nodeService) {
	    this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setCaseElementService(CaseElementServiceImpl caseElementService) {
        this.caseElementService = caseElementService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void register() {
        caseElementService.register(this);
    }

    protected List<NodeRef> filterByClass(List<NodeRef> nodeRefs, QName className) {
		List<NodeRef> result = new LinkedList<NodeRef>();
		ClassDefinition classDef = dictionaryService.getClass(className);
		if(classDef.isAspect()) {
            for(NodeRef nodeRef : nodeRefs) {
                if(nodeService.hasAspect(nodeRef, className)) {
                    result.add(nodeRef);
                }
            }
		} else {
		    for(NodeRef nodeRef : nodeRefs) {
		        if(RepoUtils.isSubType(nodeRef, className, nodeService, dictionaryService)) {
		            result.add(nodeRef);
		        }
		    }
		}
		return result;
	}

    @Override
    public List<NodeRef> getCases(NodeRef element, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        
        if (!nodeService.exists(element)) {
            throw new IllegalArgumentException("Element node does not exist: " + element);
        }
        if (!nodeService.exists(config)) {
            throw new IllegalArgumentException("Config node does not exist: " + config);
        }

        QName elementType = needElementType(config);
        if(!RepoUtils.isSubClass(element, elementType, nodeService, dictionaryService)) {
            return Collections.emptyList();
        }
        
        List<NodeRef> parents = getCasesImpl(element, config);
        return filterByClass(parents, ICaseModel.ASPECT_CASE);
    }
    
    protected abstract List<NodeRef> getCasesImpl(NodeRef element, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException;
    
    @Override
    public void addAll(Collection<NodeRef> elements, NodeRef caseNode, NodeRef config) {
        if(elements == null || elements.isEmpty()) return;
        Set<NodeRef> currentElements = new HashSet<>(get(caseNode, config));
        for (NodeRef element : elements) {
            if(!currentElements.contains(element)) {
                add(element, caseNode, config);
            }
        }
    }

	@Override
	public NodeRef destination(NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException {
	
		if (!nodeService.exists(caseNode)) {
			throw new IllegalArgumentException("Cannot create the association without caseNode");
		}
		if (!nodeService.exists(config)) {
			throw new IllegalArgumentException("Cannot create the association without config");
		}
		try {
			NodeRef result = CaseUtils.getCaseFolder(caseNode, config, nodeService);
			if (result == null)
				result = CaseUtils.createCaseFolder(caseNode, config, nodeService);
			return result;
		}
		catch(IllegalArgumentException e) {
			throw e;
		}
		catch(Throwable e) {
			throw new AlfrescoRuntimeException("Can not get elements from case. caseNode=" + caseNode + "; config=" + config, e);
		}
	}
	
    @Override
    public void copyElementsToTemplate(NodeRef caseNodeRef, NodeRef template, NodeRef config) {
        List<NodeRef> elements = get(caseNodeRef, config);
        for(NodeRef element : elements) {
            nodeService.createAssociation(template, element, ICaseTemplateModel.ASSOC_EXTERNAL_ELEMENTS);
        }
    }
    
    @Override
    public void copyElementsFromTemplate(NodeRef template, NodeRef caseNodeRef, NodeRef config) {
        List<NodeRef> elements = RepoUtils.getTargetNodeRefs(template, ICaseTemplateModel.ASSOC_EXTERNAL_ELEMENTS, nodeService);
        addAll(elements, caseNodeRef, config);
    }

    protected QName needElementType(NodeRef config) {
        return RepoUtils.getMandatoryProperty(config, ICaseModel.PROP_ELEMENT_TYPE, nodeService);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + getElementConfigType() + ")";
    }
	
}
