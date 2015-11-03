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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.xml.Mode;
import ru.citeck.ecos.invariants.xml.View;
import ru.citeck.ecos.invariants.xml.Views;

public final class NodeView extends NodeViewElement {

    private static final String FIELD_CLASS = "class";
    private static final String FIELD_MODE = "mode";
    
    private static final List<Key> DEFAULT_KEYS = Collections.singletonList(new Key(NodeView.class, FIELD_CLASS, ANY_QNAME, null, null));
    
    private QName className;
    private NodeViewMode mode;
    
    private NodeView() {}

    public QName getClassName() {
        return className;
    }

    public NodeViewMode getMode() {
        return mode;
    }

    public List<NodeViewElement> getElements() {
        return super.getElements();
    }
    
    public List<InvariantDefinition> getFieldInvariants() {
        List<InvariantDefinition> invariants = new LinkedList<>();
        getFieldInvariants(this, invariants);
        return invariants;
    }
    
    private static void getFieldInvariants(NodeViewElement element, List<InvariantDefinition> invariants) {
        if(element instanceof NodeField) {
            invariants.addAll(((NodeField) element).getInvariants());
        }
        for(NodeViewElement child : element.getElements()) {
            getFieldInvariants(child, invariants);
        }
    }
    
    @Override
    /*package*/ List<Key> getKeys() {
        if(className != null && mode != null) {
            List<Key> keys = new ArrayList<>(2);
            keys.add(new Key(NodeView.class, FIELD_CLASS, className, id, mode));
            keys.add(new Key(NodeView.class, FIELD_CLASS, className, id, null));
            return keys;
        } else if(className != null && mode == null) {
            return Collections.singletonList(new Key(NodeView.class, FIELD_CLASS, className, id, null));
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    /*package*/ List<Key> getNonKeys() {
        List<Key> keys = new ArrayList<>(4);
        if(id != null) keys.add(new Key(NodeView.class, FIELD_ID, id));
        if(mode != null) keys.add(new Key(NodeView.class, FIELD_MODE, mode));
        if(kind != null) keys.add(new Key(NodeView.class, FIELD_KIND, kind));
        if(template != null) keys.add(new Key(NodeView.class, FIELD_TEMPLATE, template));
        return keys;
    }
    
    @Override
    /*package*/ List<Key> getDefaultKeys() {
        return DEFAULT_KEYS;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(concrete ? "view[" : "views[");
        if(any) sb.append("any,");
        if(className != null) 
            sb.append("class=")
              .append(className.toPrefixString())
              .append(",");
        if(id != null)
            sb.append("id=")
              .append(id)
              .append(",");
        if(mode != null)
            sb.append("mode=")
              .append(mode)
              .append(",");
        if(kind != null)
            sb.append("kind=")
              .append(kind)
              .append(",");
        if(template != null)
            sb.append("template=")
              .append(template)
              .append(params);
        return sb.append("]").toString();
    }

    @Override
    public <T> T visit(NodeViewElementVisitor<T> visitor)  {
        return visitor.visit(this);
    }

    public static final class Builder extends NodeViewElement.Builder<Builder> {
        
        private QName className;
        private NodeViewMode mode;
        
        public Builder(NamespacePrefixResolver prefixResolver) {
            super(prefixResolver);
        }

        public Builder className(String className) {
            return className(className != null 
                    ? QName.createQName(className, prefixResolver)
                    : null);
        }
        
        public Builder className(QName className) {
            this.className = className;
            return this;
        }
        
        public Builder mode(String mode) {
            this.mode = mode != null 
                    ? NodeViewMode.valueOf(mode.toUpperCase())
                    : null;
            return this;
        }
        
        public Builder mode(Mode mode) {
            this.mode = mode != null
                    ? NodeViewMode.valueOf(mode.toString())
                    : null;
            return this;
        }
        
        public Builder mode(NodeViewMode mode) {
            this.mode = mode;
            return this;
        }
        
        public Builder elements(List<? extends NodeViewElement> elements) {
            return super.elements(elements);
        }
        
        public Builder data(View data) {
            return super.data(data)
                    .className(data.getClazz())
                    .mode(data.getMode())
                    .elements(buildElements(data.getElements(), prefixResolver));
        }
        
        public Builder data(Views data) {
            return data((View) data)
                    .concrete(false)
                    .any(data.isAny());
        }
        
        public Builder reset() {
            super.reset();
            className = null;
            mode = null;
            return this;
        }
        
        public Builder merge(NodeView view) {
            super.merge(view);
            if(className == null) className = view.getClassName();
            if(mode == null) mode = view.getMode();
            return this;
        }
        
        @Override 
        public Builder merge(NodeViewElement element) {
            if(element instanceof NodeView) {
                return merge((NodeView) element);
            } else {
                return this;
            }
        }
        
        @Override
        public NodeView build() {
            NodeView result = new NodeView();
            super.init(result);
            result.className = className;
            result.mode = mode;
            return result;
        }

    }

}
