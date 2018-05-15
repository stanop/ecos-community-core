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
package ru.citeck.ecos.icase.element;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;

public interface CaseElementService {

    /**
     * Get all element types, registered in system.
     * 
     * @return
     */
    List<String> getAllElementTypes();

    /**
     * Get all element types, valid for case.
     * 
     * @param caseNodeRef
     * @return
     */
    List<String> getAllElementTypes(NodeRef caseNodeRef);
    
    /**
     * Get elements of case.
     * 
     * @param caseNodeRef
     * @param elementType
     * @return
     */
    List<NodeRef> getElements(NodeRef caseNodeRef, String elementType);
    
    /**
     * Get cases, in which the specified node is added as an element.
     * 
     * @param nodeRef
     * @param elementType
     * @return
     */
    List<NodeRef> getCases(NodeRef nodeRef, String elementType);

    /**
     * Add element to case.
     * 
     * @param element - element to be added
     * @param caseNodeRef - case node reference
     * @param elementType - name of the element type configuration node
     * @throws AlfrescoRuntimeException
     */
    void addElement(NodeRef element, NodeRef caseNodeRef, String elementType)
            throws AlfrescoRuntimeException;

    /**
     * Add elements to case.
     * 
     * @param elements - elements to be added
     * @param caseNodeRef - case node reference
     * @param elementType - name of the element type configuration node
     * @throws AlfrescoRuntimeException
     */
    void addElements(Collection<NodeRef> elements, NodeRef caseNodeRef, String elementType)
            throws AlfrescoRuntimeException;

    /**
     * Remove element from case.
     * 
     * @param nodeRef - input node reference
     * @param caseNodeRef - case node reference
     * @param elementType - name of element type configuration node
     * @throws AlfrescoRuntimeException
     */
    void removeElement(NodeRef nodeRef, NodeRef caseNodeRef, String elementType)
            throws AlfrescoRuntimeException;
    
    /**
     * It returns node reference of destination of case elements.
     * 
     * @param caseNodeRef - case node reference
     * @param elementType - name of element type configuration node
     * @return
     * @throws AlfrescoRuntimeException
     */
    NodeRef destination(NodeRef caseNodeRef, String elementType)
            throws AlfrescoRuntimeException;

    /**
     * Copy case configuration to specified template.
     *  
     * @param caseNodeRef
     * @param templateRef
     */
    void copyCaseToTemplate(NodeRef caseNodeRef, NodeRef templateRef);
    
    /**
     * Copy case configuration from specified template.
     *  
     * @param templateRef
     * @param caseNodeRef
     */
    void copyTemplateToCase(NodeRef templateRef, NodeRef caseNodeRef);


    /**
     * Get elements config by name
     */
    Optional<ElementConfigDto> getConfig(String configName);

    /**
     * Get element config by config nodeRef
     */
    Optional<ElementConfigDto> getConfig(NodeRef nodeRef);
}
