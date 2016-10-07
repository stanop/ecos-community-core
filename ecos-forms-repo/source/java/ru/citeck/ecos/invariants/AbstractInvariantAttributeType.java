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

import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.attr.NodeAttributeService;

public abstract class AbstractInvariantAttributeType implements InvariantAttributeType {

    protected NamespacePrefixResolver prefixResolver;
    protected DictionaryService dictionaryService;
    protected MessageLookup messageLookup;
    private NodeAttributeService nodeAttributeService;
    private Map<QName, InvariantAttributeType> registry;

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setMessageLookup(MessageLookup messageLookup) {
        this.messageLookup = messageLookup;
    }

    public void setNodeAttributeService(NodeAttributeService nodeAttributeService) {
        this.nodeAttributeService = nodeAttributeService;
    }

    public void setRegistry(Map<QName, InvariantAttributeType> registry) {
        this.registry = registry;
    }

    public void init() {
        registry.put(this.getSupportedAttributeType(), this);
    }

    @Override
    public QName getAttributeSubtype(QName attributeName) {
        return nodeAttributeService.getAttributeSubtype(attributeName);
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        return nodeAttributeService.getAttributeValueType(attributeName);
    }
}
