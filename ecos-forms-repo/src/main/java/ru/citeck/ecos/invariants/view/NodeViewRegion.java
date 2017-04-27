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
import java.util.List;

import org.alfresco.service.namespace.NamespacePrefixResolver;

import ru.citeck.ecos.invariants.xml.Region;
import ru.citeck.ecos.invariants.xml.Regions;

public final class NodeViewRegion extends NodeViewElement {

    private static final String FIELD_NAME = "name";
    
    private static final List<Key> DEFAULT_KEYS = Collections.singletonList(new Key(NodeViewRegion.class, FIELD_NAME, ANY_NAME, null, null));
    
    private String name;
    
    private NodeViewRegion() {}

    public String getName() {
        return name;
    }

    @Override
    /*package*/ List<Key> getKeys() {
        if(name != null) {
            return Collections.singletonList(new Key(NodeViewRegion.class, FIELD_NAME, name, id, null));
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    /*package*/ List<Key> getNonKeys() {
        List<Key> keys = new ArrayList<>(3);
        if(id != null) keys.add(new Key(NodeViewRegion.class, FIELD_ID, id));
        if(kind != null) keys.add(new Key(NodeViewRegion.class, FIELD_KIND, kind));
        if(template != null) keys.add(new Key(NodeViewRegion.class, FIELD_TEMPLATE, template));
        return keys;
    }
    
    @Override
    /*package*/ List<Key> getDefaultKeys() {
        return DEFAULT_KEYS;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(concrete ? "region[" : "regions[");
        if(any) sb.append("any,");
        if(name != null) 
            sb.append("name=")
              .append(name)
              .append(",");
        if(id != null)
            sb.append("id=")
              .append(id)
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
    public <T> T visit(NodeViewElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public static final class Builder extends NodeViewElement.Builder<Builder> {
        
        private String name;
        
        public Builder(NamespacePrefixResolver prefixResolver) {
            super(prefixResolver);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        @Override
        public Builder reset() {
            super.reset();
            name = null;
            return this;
        }
        
        public Builder merge(NodeViewRegion region) {
            super.merge(region);
            if(name == null) name = region.getName();
            return this;
        }
        
        @Override 
        public Builder merge(NodeViewElement element) {
            if(element instanceof NodeViewRegion) {
                return merge((NodeViewRegion) element);
            } else {
                return this;
            }
        }
        
        public Builder data(Region data) {
            return super.data(data)
                    .name(data.getName());
        }
        
        public Builder data(Regions data) {
            return this.data((Region) data)
                    .concrete(false)
                    .any(data.isAny());
        }
        
        @Override
        public NodeViewRegion build() {
            NodeViewRegion result = new NodeViewRegion();
            super.init(result);
            result.name = name;
            return result;
        }

    }
    
}
