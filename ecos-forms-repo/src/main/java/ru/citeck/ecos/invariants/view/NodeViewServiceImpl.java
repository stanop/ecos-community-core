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
package ru.citeck.ecos.invariants.view;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.invariants.InvariantConstants;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantService;
import ru.citeck.ecos.model.AttributeModel;

class NodeViewServiceImpl implements NodeViewService {

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeAttributeService nodeAttributeService;
    private InvariantService invariantService;
    private NodeViewsParser parser;
    private NodeViewsFilter filter;
    private static NodeRef defaultParent = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "attachments-root");
    private static Map<QName, QName> defaultParentAssocs = new HashMap<>(2);
    static {
        defaultParentAssocs.put(ContentModel.TYPE_CONTAINER, ContentModel.ASSOC_CHILDREN);
        defaultParentAssocs.put(ContentModel.TYPE_FOLDER, ContentModel.ASSOC_CONTAINS);
        defaultParentAssocs.put(WorkflowModel.ASPECT_WORKFLOW_PACKAGE, WorkflowModel.ASSOC_PACKAGE_CONTAINS);
    }

    @Override
    public void deployDefinition(InputStream definition, String sourceId) {
        List<NodeViewElement> elements = parser.parse(definition);
        filter.registerViews(elements, sourceId);
    }

    @Override
    public void undeployDefinition(String sourceId) {
        filter.unregisterViews(sourceId);
    }

    @Override
    public NodeView getNodeView(QName className) {
        return getNodeView(className, null);
    }

    @Override
    public NodeView getNodeView(QName className, String id) {
        return filter.resolveView(new NodeView.Builder(namespaceService)
                .className(className)
                .id(id)
                .mode(NodeViewMode.CREATE)
                .build());
    }

    @Override
    public NodeView getNodeView(NodeRef nodeRef) {
        return getNodeView(nodeRef, null);
    }

    @Override
    public NodeView getNodeView(NodeRef nodeRef, String id) {
        QName typeName = nodeService.getType(nodeRef);
        return filter.resolveView(new NodeView.Builder(namespaceService)
                .className(typeName)
                .id(id)
                .mode(NodeViewMode.EDIT)
                .build());
    }

    @Override
    public NodeView getNodeView(NodeView view) {
        return filter.resolveView(view);
    }

    @Override
    public boolean hasNodeView(NodeView view) {
        return filter.isViewRegistered(view);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeAttributeService(NodeAttributeService nodeAttributeService) {
        this.nodeAttributeService = nodeAttributeService;
    }

    public void setInvariantService(InvariantService invariantService) {
        this.invariantService = invariantService;
    }

    public void setParser(NodeViewsParser parser) {
        this.parser = parser;
    }

    public void setFilter(NodeViewsFilter filter) {
        this.filter = filter;
    }

    @Override
    public NodeRef saveNodeView(QName typeQName, Map<QName, Object> attributes) {
        return this.saveNodeView(typeQName, null, attributes, null);
    }

    @Override
    public void saveNodeView(NodeRef nodeRef, Map<QName, Object> attributes) {
        this.saveNodeView(nodeRef, null, attributes, null);
    }

    @Override
    public void saveNodeView(NodeRef nodeRef, String id, Map<QName, Object> attributes, Map<String, Object> params) {
        nodeAttributeService.setAttributes(nodeRef, attributes);

        NodeView view = filter.resolveView(new NodeView.Builder(namespaceService)
                .className(nodeService.getType(nodeRef))
                .id(id)
                .mode(NodeViewMode.EDIT)
                .templateParams(params)
                .build());
        executeInvariants(nodeRef, view);
    }

    @Override
    public NodeRef saveNodeView(QName typeQName, String id, Map<QName, Object> attributes, Map<String, Object> params) {
        if (typeQName.equals(ContentModel.TYPE_PERSON)) {
            params.put("destination", null);
        }
        Map<QName, Object> effectiveAttributes = new HashMap<>(attributes.size() + 3);
        effectiveAttributes.putAll(attributes);
        if (effectiveAttributes.get(AttributeModel.ATTR_TYPES) == null) {
            effectiveAttributes.put(AttributeModel.ATTR_TYPES, Collections.singletonList(typeQName));
        }
        if (effectiveAttributes.get(AttributeModel.ATTR_PARENT) == null &&
            !typeQName.equals(ContentModel.TYPE_PERSON)) {
            effectiveAttributes.put(AttributeModel.ATTR_PARENT, defaultParent);
        }
        if (effectiveAttributes.get(AttributeModel.ATTR_PARENT_ASSOC) == null &&
            !typeQName.equals(ContentModel.TYPE_PERSON)) {
            Object parentObj = effectiveAttributes.get(AttributeModel.ATTR_PARENT);
            NodeRef parent = parentObj instanceof NodeRef ? (NodeRef) parentObj :
                    parentObj instanceof String ? new NodeRef((String) parentObj) :
                            null;
            QName parentType = nodeService.getType(parent);
            for (QName className : defaultParentAssocs.keySet()) {
                if (dictionaryService.isSubClass(parentType, className) || nodeService.hasAspect(parent, className)) {
                    effectiveAttributes.put(AttributeModel.ATTR_PARENT_ASSOC, defaultParentAssocs.get(className));
                    break;
                }
            }
        }
        NodeRef nodeRef = nodeAttributeService.persistAttributes(effectiveAttributes);

        NodeView view = filter.resolveView(new NodeView.Builder(namespaceService)
                .className(typeQName)
                .id(id)
                .mode(NodeViewMode.CREATE)
                .templateParams(params)
                .build());
        executeInvariants(nodeRef, view);
        return nodeRef;
    }

    // TODO move execute invariants to behaviour and/or integrity checker
    private void executeInvariants(NodeRef nodeRef, NodeView view) {
        List<InvariantDefinition> invariants = getInvariants(nodeRef, view);
        Map<String, Object> model = new HashMap<>();
        model.put(InvariantConstants.MODEL_VIEW, view);
        if(view.getMode() == NodeViewMode.CREATE)
            invariantService.executeInvariantsForNewNode(nodeRef, invariants, model);
        else
            invariantService.executeInvariants(nodeRef, invariants, model);
    }

    private List<InvariantDefinition> getInvariants(NodeRef nodeRef, NodeView view) {
        List<InvariantDefinition> nodeInvariants = invariantService.getInvariants(nodeRef);
        List<InvariantDefinition> viewInvariants = view.getFieldInvariants();
        if(viewInvariants.isEmpty()) return nodeInvariants;
        if(nodeInvariants.isEmpty()) return viewInvariants;
        List<InvariantDefinition> allInvariants = new ArrayList<>(nodeInvariants.size() + viewInvariants.size());
        allInvariants.addAll(viewInvariants);
        allInvariants.addAll(nodeInvariants);
        return allInvariants;
    }

}
