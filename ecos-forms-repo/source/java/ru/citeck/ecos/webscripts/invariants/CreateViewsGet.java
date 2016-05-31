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
package ru.citeck.ecos.webscripts.invariants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeView.Builder;
import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.utils.DictionaryUtils;

public class CreateViewsGet extends DeclarativeWebScript {

    private static final String PARAM_TYPE = "type";
    private static final String MODEL_VIEWS = "createViewClasses";
    
    private DictionaryService dictionaryService;
    private NodeViewService nodeViewService;
    private NamespacePrefixResolver prefixResolver;
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String typeParam = req.getParameter(PARAM_TYPE);
        
        if(typeParam == null || typeParam.isEmpty()) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Argument 'type' should be set");
            return null;
        }
        
        List<QName> existingViewClasses = new LinkedList<>();
        QName className = QName.resolveToQName(prefixResolver, typeParam);
        NodeView.Builder builder = new NodeView.Builder(prefixResolver)
                .mode(NodeViewMode.CREATE);
        
        if(nodeViewExists(builder, className)) {
            existingViewClasses.add(className);
        }
        
        for(QName childClassName : DictionaryUtils.getChildClassNames(className, true, dictionaryService)) {
            if(childClassName.equals(className)) continue;
            if(nodeViewExists(builder, childClassName)) {
                existingViewClasses.add(childClassName);
            }
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_VIEWS, existingViewClasses);
        return model;
    }

    private boolean nodeViewExists(Builder builder, QName className) {
        NodeView view = builder
                .className(className)
                .build();
        return nodeViewService.hasNodeView(view);
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeViewService(NodeViewService nodeViewService) {
        this.nodeViewService = nodeViewService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

}
