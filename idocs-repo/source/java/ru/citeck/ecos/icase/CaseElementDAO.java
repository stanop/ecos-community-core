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
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This interface describes strategy methods.
 * 
 * Case element strategy allows to:
 * 1) get list of case elements for the specified case and its config.
 * 2) add specified case element to specified case node and its config.
 * 3) remove specified case element from specified case node and its config.
 * 4) get destination folder, which should used for uploading of
 *    new case elements for the specified case and its config.
 * 5) copy element config from source node reference and its config to
 *    the target node reference.
 * 
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public interface CaseElementDAO {

	/**
	 * It returns list of case elements.
	 * @param caseNode - case node reference
	 * @param config - config node reference
	 * @return list of element node references
	 * @throws AlfrescoRuntimeException
	 * @throws IllegalArgumentException
	 */
	List<NodeRef> get(NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException;

	/**
	 * It returns list of cases for the element.
	 * If element is not of required type, it returns empty list.
	 * @param element - case element reference
	 * @param config - config node reference
	 * @return list of case node references
	 * @throws AlfrescoRuntimeException
	 * @throws IllegalArgumentException
	 */
	List<NodeRef> getCases(NodeRef element, NodeRef config)
	        throws AlfrescoRuntimeException, IllegalArgumentException;

    /**
     * It adds case element.
     * @param nodeRef - element node reference
     * @param caseNode - case node reference
     * @param config - config node reference
     * @throws AlfrescoRuntimeException
     * @throws IllegalArgumentException
     */
    void add(NodeRef nodeRef, NodeRef caseNode, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException;

    /**
     * It adds elements to case.
     * 
     * @param nodeRefs - elements to be added
     * @param caseNode - case node reference
     * @param config - config node reference
     * @throws AlfrescoRuntimeException
     * @throws IllegalArgumentException
     */
    void addAll(Collection<NodeRef> nodeRefs, NodeRef caseNode, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException;

	/**
	 * It removes case element.
	 * @param nodeRef - element node reference
	 * @param caseNode - case node reference
	 * @param config - config node reference
	 * @throws AlfrescoRuntimeException
	 * @throws IllegalArgumentException
	 */
	void remove(NodeRef nodeRef, NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException;

	/**
	 * It returns node reference of folder, which should used for
	 * uploading of new case elements.
	 * @param caseNode - case node reference
	 * @param config - config node reference
	 * @return
	 * @throws AlfrescoRuntimeException
	 * @throws IllegalArgumentException
	 */
	NodeRef destination(NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException;

	/**
	 * Initialize behaviours to provide onCaseElementAdd, onCaseElementRemove policies.
	 * 
	 * @param config element type configuration
	 * @return set of initialized behaviours (to be removed, when the config is changed or removed)
	 */
	Set<BehaviourDefinition<?>> intializeBehaviours(NodeRef config);

	/**
	 * Case element-config repository type.
	 * 
	 * @return type, for which this strategy applies.
	 */
	QName getElementConfigType();

}
