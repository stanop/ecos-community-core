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

import org.alfresco.repo.jscript.ScriptNode;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class NodeViewServiceJS extends AlfrescoScopableProcessorExtension {

    private NodeViewService impl;

    public boolean hasNodeView(String type, String id) {
        return impl.hasNodeView(id, type);
    }

    public boolean hasNodeView(ScriptNode node, String id) {
        return impl.hasNodeView(node, id);
    }

    public NodeView getNodeView(String className, String id) {
        return impl.getNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
            .className(className)
            .id(id)
            .build());
    }

    public NodeView getNodeView(ScriptNode node, String id) {
        return impl.getNodeView(new NodeView.Builder(serviceRegistry.getNamespaceService())
            .className(node.getTypeShort())
            .id(id)
            .build());
    }
    
    public void setImpl(NodeViewService impl) {
        this.impl = impl;
    }
}
