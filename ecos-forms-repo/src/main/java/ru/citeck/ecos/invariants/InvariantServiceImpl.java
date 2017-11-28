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
package ru.citeck.ecos.invariants;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.utils.DictionaryUtils;

class InvariantServiceImpl implements InvariantService {

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    
    // components
    private InvariantsParser parser;
    private InvariantsFilter filter;
    private InvariantsRuntime runtime;
    private Map<String, InvariantLanguage> languages;
    private Map<QName, InvariantAttributeType> attributeTypes;

    @Override
    public void deployDefinition(InputStream source, String sourceId, InvariantPriority priority) {
        List<InvariantDefinition> invariants = parser.parse(source, priority);
        filter.registerInvariants(invariants, sourceId);
    }

    @Override
    public void undeployDefinition(String sourceId) {
        filter.unregisterInvariants(sourceId);
    }

    @Override
    public List<InvariantDefinition> getInvariants(QName className) {
        return filter.searchMatchingInvariants(Collections.singletonList(className));
    }

    @Override
    public List<InvariantDefinition> getInvariants(NodeRef nodeRef) {
        List<QName> classNames = new LinkedList<>();
        classNames.add(nodeService.getType(nodeRef));
        classNames.addAll(nodeService.getAspects(nodeRef));
        return filter.searchMatchingInvariants(classNames);
    }

    @Override
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames, Collection<QName> attributeNames) {
        return filter.searchMatchingInvariants(classNames, attributeNames, true);
    }

    @Override
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames, Collection<QName> attributeNames,
                                                   NodeRef nodeRef, String mode) {
        return filter.searchMatchingInvariants(classNames, attributeNames, true, nodeRef, mode);
    }

    @Override
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames, Collection<QName> attributeNames,
                                                   NodeRef nodeRef, NodeRef baseRef, String mode) {
        return filter.searchMatchingInvariants(classNames, attributeNames, true, nodeRef, baseRef, mode);
    }

    @Override
    public List<InvariantDefinition> getInvariants(QName className, Collection<QName> attributeNames) {
        return filter.searchMatchingInvariants(Collections.singleton(className), attributeNames, true);
    }

    @Override
    public List<InvariantDefinition> getInvariants(NodeRef nodeRef,
            Collection<QName> attributeNames) {
        Collection<QName> classNames = new LinkedHashSet<>();
        classNames.add(nodeService.getType(nodeRef));
        classNames.addAll(nodeService.getAspects(nodeRef));
        classNames.addAll(DictionaryUtils.getDefiningClassNames(attributeNames, dictionaryService));
        return filter.searchMatchingInvariants(classNames);
    }

    @Override
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames) {
        return filter.searchMatchingInvariants(classNames);
    }

    @Override
    public void registerLanguage(InvariantLanguage language) {
        languages.put(language.getName(), language);
    }

    @Override
    public Set<String> getSupportedLanguages() {
        return languages.keySet();
    }
    
    @Override
    public void registerAttributeType(InvariantAttributeType attributeType) {
        attributeTypes.put(attributeType.getSupportedAttributeType(), attributeType);
    }
    
    @Override
    public Set<QName> getSupportedAttributeTypes() {
        return attributeTypes.keySet();
    }

    @Override
    public Object evaluateInvariant(InvariantDefinition invariant,
            Map<String, Object> model) {
        return runtime.evaluateInvariant(invariant, model);
    }

    @Override
    public void executeInvariants(NodeRef nodeRef) {
        List<InvariantDefinition> invariants = this.getInvariants(nodeRef);
        executeInvariants(nodeRef, invariants, null);
    }

    @Override
    public void executeInvariantsForNewNode(NodeRef nodeRef) {
        List<InvariantDefinition> invariants = this.getInvariants(nodeRef);
        executeInvariantsForNewNode(nodeRef, invariants, null);
    }

    @Override
    public void executeInvariants(NodeRef nodeRef, List<InvariantDefinition> invariants, Map<String, Object> model) {
        runtime.executeInvariants(nodeRef, invariants, model, false);
    }

    @Override
    public void executeInvariantsForNewNode(NodeRef nodeRef, List<InvariantDefinition> invariants, Map<String, Object> model) {
        runtime.executeInvariants(nodeRef, invariants, model, true);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setParser(InvariantsParser parser) {
        this.parser = parser;
    }

    public void setFilter(InvariantsFilter filter) {
        this.filter = filter;
    }

    public void setRuntime(InvariantsRuntime runtime) {
        this.runtime = runtime;
    }

    public void setLanguagesRegistry(Map<String, InvariantLanguage> languages) {
        this.languages = languages;
    }

    public void setAttributeTypesRegistry(Map<QName, InvariantAttributeType> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

}
