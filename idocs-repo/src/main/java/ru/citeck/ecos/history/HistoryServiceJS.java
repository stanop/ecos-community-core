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
package ru.citeck.ecos.history;

import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.ScriptableObject;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Javascript wrapper for HistoryService.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class HistoryServiceJS extends BaseProcessorExtension {

    private HistoryService historyService;

    private DictionaryNamespaceComponent namespaceService;

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public void setNamespaceService(DictionaryNamespaceComponent namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void persistEvent(String type, Object properties) {
        if (properties instanceof ScriptableObject) {
            Map<QName, Serializable> propertiesMap = new HashMap<QName, Serializable>();
            JavaScriptImplUtils.extractPropertiesMap((ScriptableObject) properties, propertiesMap, new ValueConverter(), namespaceService);
            QName typeQName = JavaScriptImplUtils.convertQName(type, namespaceService);
            historyService.persistEvent(typeQName, propertiesMap);
        }
    }
    
    public void addHistoricalProperty(ScriptNode node, String sourceProp, String historyProp) {
        QName sourcePropQName = QName.resolveToQName(namespaceService, sourceProp);
        QName historyPropQName = QName.resolveToQName(namespaceService, historyProp);
        historyService.addHistoricalProperty(node.getNodeRef(), sourcePropQName, historyPropQName);
    }

    public String sendAndRemoveAllOldEvents(Integer offset, Integer maxItemsCount, Integer stopCount) {
        return historyService.sendAndRemoveAllOldEvents(offset, maxItemsCount, stopCount);
    }

    public String interruptHistoryTransferring() {
        return historyService.interruptHistoryTransferring();
    }

    public void sendAndRemoveOldEventsByDocument(ScriptNode node) {
        NodeRef documentRef = node.getNodeRef();
        historyService.sendAndRemoveOldEventsByDocument(documentRef);
    }

    public void removeEventsByDocument(ScriptNode node) {
        NodeRef documentRef = node.getNodeRef();
        historyService.removeEventsByDocument(documentRef);
    }

}
