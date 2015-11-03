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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ru.citeck.ecos.invariants.xml.Element;
import ru.citeck.ecos.invariants.xml.Field;
import ru.citeck.ecos.invariants.xml.Fields;
import ru.citeck.ecos.invariants.xml.Param;
import ru.citeck.ecos.invariants.xml.Region;
import ru.citeck.ecos.invariants.xml.Regions;
import ru.citeck.ecos.invariants.xml.View;
import ru.citeck.ecos.invariants.xml.Views;

public abstract class NodeViewElement {
    
    protected static final String ANY_NAME = "any";
    protected static final QName ANY_QNAME = QName.createQName(null, ANY_NAME);

    protected static final String FIELD_ID = "id";
    protected static final String FIELD_KIND = "kind";
    protected static final String FIELD_TEMPLATE = "template";

    protected String id;
    protected String kind;
    protected String template;
    protected boolean concrete;
    protected boolean any;
    protected Map<String, Object> params;
    
    protected List<NodeViewElement> elements;
    
    /*package*/ NodeViewElement() {}
    
    /*package*/ List<NodeViewElement> getElements() {
        return elements != null 
                ? Collections.<NodeViewElement>unmodifiableList(elements) 
                : Collections.<NodeViewElement>emptyList();
    }
    
    public String getId() {
        return id;
    }
    
    public String getKind() {
        return kind;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public boolean isConcrete() {
        return concrete;
    }
    
    /*package*/ abstract List<Key> getKeys();
    /*package*/ abstract List<Key> getNonKeys();
    /*package*/ abstract List<Key> getDefaultKeys();
    
    /*package*/ List<Key> getSearchKeys() {
        if(any) return getDefaultKeys();
        List<Key> keys = getKeys();
        List<Key> nonKeys = getNonKeys();
        List<Key> defaultKeys = getDefaultKeys();
        List<Key> searchKeys = new ArrayList<>(keys.size() + nonKeys.size() + defaultKeys.size());
        searchKeys.addAll(keys);
        searchKeys.addAll(nonKeys);
        searchKeys.addAll(defaultKeys);
        return searchKeys;
    }
    
    /*package*/ List<Key> getIndexKeys() {
        if(any) return getDefaultKeys();
        List<Key> keys = getKeys();
        if(!keys.isEmpty()) return keys.subList(0, 1);
        List<Key> nonKeys = getNonKeys();
        if(!nonKeys.isEmpty()) return nonKeys.subList(0, 1);
        return Collections.emptyList();
    }
    
    /*package*/ Key getKey() {
        if(any) return null;
        List<Key> keys = getKeys();
        return keys.isEmpty()
                ? null
                : keys.get(0);
    }
    
    public String toFullString() {
        StringBuilder sb = new StringBuilder().append(toString());
        if(elements.size() > 0) {
            sb.append(":elements=[\n");
            for(NodeViewElement element : elements) {
                sb.append("\t").append(element).append("\n");
            }
        }
        return sb.toString();
    }

    public abstract <T> T visit(NodeViewElementVisitor<T> visitor);
    
    /*package*/ static final class Key implements Comparable<Key> {
        
        final Class<? extends NodeViewElement> clazz;
        final String field;
        final Object value;
        final Object[] values;
        
        Key(Class<? extends NodeViewElement> clazz, String field, Object value, Object... values) {
            this.clazz = clazz;
            this.field = field;
            this.value = value;
            this.values = values;
        }
        
        @Override
        public boolean equals(Object object) {
            if(!(object instanceof Key)) return false;
            Key that = (Key) object;
            return new EqualsBuilder()
                    .append(this.clazz, that.clazz)
                    .append(this.field, that.field)
                    .append(this.value, that.value)
                    .append(this.values, that.values)
                    .isEquals();
        }
        
        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(clazz)
                    .append(field)
                    .append(value)
                    .append(values)
                    .toHashCode();
        }
        
        @Override
        public String toString() {
            return new StringBuilder()
                    .append("{")
                    .append(clazz.getSimpleName())
                    .append(":")
                    .append(field)
                    .append("=")
                    .append(value)
                    .append(values == null ? "" : "," + Arrays.toString(values))
                    .append("}")
                    .toString();
        }
        
        @Override
        public int compareTo(Key that) {
            return new CompareToBuilder()
                    .append(this.clazz, that.clazz)
                    .append(this.field, that.field)
                    .append(this.value, that.value)
                    .append(this.values, that.values)
                    .toComparison();
        }
    }
    
    @SuppressWarnings("unchecked")
    /*package*/ static abstract class Builder<B extends Builder<?>> {
        
        protected NamespacePrefixResolver prefixResolver;
        protected DictionaryService dictionaryService;
        
        protected String id;
        protected String kind;
        protected String template;
        protected Map<String, Object> params;
        protected List<NodeViewElement> elements;
        protected boolean concrete = true;
        protected boolean any = false;
        
        public Builder(NamespacePrefixResolver prefixResolver) {
            this.prefixResolver = prefixResolver;
        }
        
        public B dictionary(DictionaryService dictionaryService) {
            this.dictionaryService = dictionaryService;
            return (B) this;
        }
        
        public B id(String id) {
            this.id = id == null || id.isEmpty() ? null : id;
            return (B) this;
        }
        
        public B kind(String kind) {
            this.kind = kind == null || kind.isEmpty() ? null : kind;
            return (B) this;
        }
        
        public B template(String template) {
            this.template = template;
            return (B) this;
        }
        
        public B templateParams(Map<String, Object> params) {
            this.params = new HashMap<>(params);
            return (B) this;
        }
        
        public B concrete(boolean concrete) {
            this.concrete = concrete;
            if(concrete) any = false;
            return (B) this;
        }
        
        public B any(boolean any) {
            this.any = any;
            if(any) concrete = false;
            return (B) this;
        }
        
        protected B data(Element data) {
            return (B) this
                    .id(data.getId())
                    .kind(data.getKind())
                    .template(data.getTemplate())
                    .templateParams(getParamMap(data.getParam()));
        }
        
        protected B elements(List<? extends NodeViewElement> elements) {
            this.elements = elements != null ? new ArrayList<>(elements) : null;
            return (B) this;
        }
        
        private Map<String, Object> getParamMap(List<Param> params) {
            Map<String, Object> paramMap = new HashMap<>(params.size());
            for(Param param : params) {
                paramMap.put(param.getName(), param.getValue());
            }
            return paramMap;
        }

        protected B reset() {
            id = null;
            kind = null;
            template = null;
            params = null;
            elements = null;
            concrete = true;
            any = false;
            return (B) this;
        }
        
        public B merge(NodeViewElement element) {
            if(id == null) id = element.getId();
            if(kind == null) kind = element.getKind();
            if(template == null) template = element.getTemplate();
            // merge params
            Map<String, Object> params2 = element.getParams();
            if(params == null || params.isEmpty()) {
                params = params2;
            } else if(params2 != null && !params2.isEmpty()) {
                Map<String, Object> params3 = new HashMap<>(params.size() + params2.size());
                params3.putAll(params2);
                params3.putAll(params); // first parameters override second
                params = params3;
            }
            return (B) this;
        }
        
        public abstract NodeViewElement build();
        
        protected void init(NodeViewElement element) {
            element.id = id;
            element.kind = kind;
            element.template = template;
            element.params = params != null ? params : Collections.<String, Object>emptyMap();
            element.elements = elements != null ? elements : Collections.<NodeViewElement>emptyList();
            element.concrete = concrete;
            element.any = any;
        }
        
        public static Builder<?> getBuilder(final NodeViewElement element, final NamespacePrefixResolver prefixResolver) {
            return element.visit(new NodeViewElementVisitor<Builder<?>>() {
                public Builder<?> visit(NodeView view) {
                    return new NodeView.Builder(prefixResolver).merge((NodeView) element);
                }
                public Builder<?> visit(NodeField field) {
                    return new NodeField.Builder(prefixResolver).merge((NodeField) element);
                }
                public Builder<?> visit(NodeViewRegion region) {
                    return new NodeViewRegion.Builder(prefixResolver).merge((NodeViewRegion) element);
                }
            });
        }
        
        public static List<NodeViewElement> buildElements(List<? extends Element> elements, NamespacePrefixResolver prefixResolver) {
            List<NodeViewElement> results = new ArrayList<>(elements.size());
            NodeView.Builder viewBuilder = new NodeView.Builder(prefixResolver);
            NodeField.Builder fieldBuilder = new NodeField.Builder(prefixResolver);
            NodeViewRegion.Builder regionBuilder = new NodeViewRegion.Builder(prefixResolver);
            for(Element element : elements) {
                NodeViewElement result = null;
                if(element == null) {
                    continue;
                } else if(element instanceof Views) {
                    result = viewBuilder.reset().data((Views) element).build();
                } else if(element instanceof View) {
                    viewBuilder.reset();
                    result = viewBuilder.data((View) element).build();
                } else if(element instanceof Fields) {
                    result = fieldBuilder.reset().data((Fields) element).build();
                } else if(element instanceof Field) {
                    result = fieldBuilder.reset().data((Field) element).build();
                } else if(element instanceof Regions) {
                    result = regionBuilder.reset().data((Regions) element).build();
                } else if(element instanceof Region) {
                    result = regionBuilder.reset().data((Region) element).build();
                } else {
                    throw new IllegalStateException("Unsupported element class: " + element.getClass());
                }
                if(result != null) {
                    results.add(result);
                }
            }
            return results;
        }
    }

}
