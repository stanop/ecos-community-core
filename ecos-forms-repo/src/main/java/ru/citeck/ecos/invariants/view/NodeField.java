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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantPriority;
import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.invariants.xml.Field;
import ru.citeck.ecos.invariants.xml.Fields;

public final class NodeField extends NodeViewElement {

    private static final String FIELD_ATTRIBUTE = "attribute";
    private static final String FIELD_DATATYPE = "datatype";
    private static final String FIELD_NODETYPE = "nodetype";
    
    private static final List<Key> DEFAULT_KEYS = Collections.singletonList(new Key(NodeField.class, FIELD_ATTRIBUTE, ANY_QNAME, null, null));

    private enum AttributeType {
        PROPERTY {
            @Override
            public String toString() {
                return "prop";
            }
        },
        ASSOCIATION {
            @Override
            public String toString() {
                return "assoc";
            }
        };
    }
    
    private QName attribute;
    private AttributeType attributeType;
    private QName datatype;
    private QName nodetype;
    
    private List<InvariantDefinition> invariants;
    
    private NodeField() {}

    public QName getAttributeName() {
        return attribute;
    }
    
    public boolean isProperty() {
        return attributeType == AttributeType.PROPERTY;
    }

    public boolean isAssociation() {
        return attributeType == AttributeType.ASSOCIATION;
    }

    public QName getDatatypeName() {
        return datatype;
    }

    public QName getNodetypeName() {
        return nodetype;
    }

    @SuppressWarnings("unchecked")
    public List<NodeViewRegion> getRegions() {
        return elements != null 
                ? Collections.<NodeViewRegion>unmodifiableList((List<NodeViewRegion>) (List<?>) elements) 
                : Collections.<NodeViewRegion>emptyList();
    }
    
    public List<InvariantDefinition> getInvariants() {
        return invariants != null 
                ? Collections.<InvariantDefinition>unmodifiableList(invariants)
                : Collections.<InvariantDefinition>emptyList();
    }

    @Override
    /*package*/ List<Key> getKeys() {
        if(attribute != null) {
            return Collections.singletonList(new Key(NodeField.class, FIELD_ATTRIBUTE, attribute, id, null));
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    /*package*/ List<Key> getNonKeys() {
        List<Key> keys = new ArrayList<>(5);
        if(id != null) keys.add(new Key(NodeField.class, FIELD_ID, id));
        if(kind != null) keys.add(new Key(NodeField.class, FIELD_KIND, kind));
        if(nodetype != null) keys.add(new Key(NodeField.class, FIELD_NODETYPE, nodetype));
        if(datatype != null) keys.add(new Key(NodeField.class, FIELD_DATATYPE, datatype));
        if(template != null) keys.add(new Key(NodeField.class, FIELD_TEMPLATE, template));
        return keys;
    }
    
    @Override
    /*package*/ List<Key> getDefaultKeys() {
        return DEFAULT_KEYS;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(concrete ? "field[" : "fields[");
        if(any) sb.append("any,");
        if(attribute != null) 
            sb.append(attributeType != null ? attributeType : "attr")
              .append("=")
              .append(attribute.toPrefixString())
              .append(",");
        if(id != null)
            sb.append("id=")
              .append(id)
              .append(",");
        if(kind != null)
            sb.append("kind=")
              .append(kind)
              .append(",");
        if(datatype != null)
            sb.append("datatype=")
              .append(datatype.toPrefixString())
              .append(",");
        if(nodetype != null)
            sb.append("nodetype=")
              .append(nodetype.toPrefixString())
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
        private QName attribute;
        private AttributeType attributeType;
        private QName datatype;
        private QName nodetype;
        private List<InvariantDefinition> invariants;
        
        public Builder(NamespacePrefixResolver prefixResolver) {
            super(prefixResolver);
        }

        public Builder property(QName name) {
            this.attribute = name;
            this.attributeType = AttributeType.PROPERTY;
            return this;
        }
        
        public Builder association(QName name) {
            this.attribute = name;
            this.attributeType = AttributeType.ASSOCIATION;
            return this;
        }
        
        public Builder datatype(QName name) {
            this.datatype = name;
            return this;
        }
        
        public Builder nodetype(QName name) {
            this.nodetype = name;
            return this;
        }
        
        public Builder property(String name) {
            return property(name == null ? null : QName.createQName(name, prefixResolver));
        }
        
        public Builder association(String name) {
            return association(name == null ? null : QName.createQName(name, prefixResolver));
        }
        
        private Builder attribute(Field data) {
            if (data.getProp() != null)
                return property(data.getProp());
            else if (data.getProperty() != null)
                return property(data.getProperty());
            else if (data.getAssoc() != null)
                return association(data.getAssoc());
            else if (data.getAssociation() != null)
                return association(data.getAssociation());
            else
                return this;
        }
        
        public Builder datatype(String name) {
            return datatype(name == null ? null : QName.createQName(name, prefixResolver));
        }
        
        public Builder nodetype(String name) {
            return nodetype(name == null ? null : QName.createQName(name, prefixResolver));
        }
        
        public Builder regions(List<? extends NodeViewRegion> regions) {
            return super.elements(regions);
        }
        
        public Builder invariants(List<InvariantDefinition> invariants) {
            this.invariants = invariants != null 
                    ? new ArrayList<InvariantDefinition>(invariants) 
                    : null;
            return this;
        }
        
        public Builder data(Field data) {
            return super.data(data)
                    .attribute(data)
                    .elements(NodeViewElement.Builder.buildElements(
                            data.getRegionOrRegions(), 
                            prefixResolver))
                    .invariants(InvariantDefinition.Builder.buildInvariants(
                            getAttributeScope(), 
                            getAttributeScopeKind(), 
                            InvariantPriority.VIEW_SCOPED, 
                            data.getInvariant(), 
                            prefixResolver));
        }
        
        public Builder data(Fields data) {
            return this.data((Field) data)
                    .concrete(false)
                    .any(data.isAny())
                    .datatype(data.getDatatype())
                    .nodetype(data.getNodetype());
        }
        
        private QName getAttributeScope() {
            return attribute != null ? attribute
                 : datatype != null ? datatype
                 : nodetype != null ? nodetype
                 : null;
        }
        
        private AttributeScopeKind getAttributeScopeKind() {
            if(attribute != null) {
                return attributeType == AttributeType.PROPERTY ? AttributeScopeKind.PROPERTY
                     : attributeType == AttributeType.ASSOCIATION ? AttributeScopeKind.ASSOCIATION
                     : null;
            } else {
                return datatype != null ? AttributeScopeKind.PROPERTY_TYPE
                     : nodetype != null ? AttributeScopeKind.ASSOCIATION_TYPE
                     : null;
            }
        }
        
        @Override
        public Builder reset() {
            super.reset();
            attribute = null;
            attributeType = null;
            datatype = null;
            nodetype = null;
            invariants = null;
            return this;
        }
        
        public Builder merge(NodeField field) {
            super.merge(field);
            if(attribute == null) attribute = field.getAttributeName();
            if(attributeType == null) attributeType = 
                    field.isProperty() ? AttributeType.PROPERTY : 
                    field.isAssociation() ? AttributeType.ASSOCIATION : 
                    null;
            if(datatype == null) datatype = field.getDatatypeName();
            if(nodetype == null) nodetype = field.getNodetypeName();
            if(!field.getInvariants().isEmpty()) {
                if(invariants == null) {
                    invariants = new LinkedList<>();
                }
                for(InvariantDefinition invariant : field.getInvariants()) {
                    if(!invariants.contains(invariant)) {
                        invariants.add(invariant);
                    }
                }
            }
            return this;
        }
        
        @Override 
        public Builder merge(NodeViewElement element) {
            if(element instanceof NodeField) {
                return merge((NodeField) element);
            } else {
                return this;
            }
        }
        
        @Override
        public NodeField build() {
            NodeField result = new NodeField();
            super.init(result);
            
            result.attribute = attribute;
            result.attributeType = attributeType;
            
            result.datatype = datatype;
            result.nodetype = nodetype;
            
            if(attribute != null && !attribute.equals(ANY_QNAME)) {
                
                if(attributeType == null) {
                    throw new IllegalStateException("You should specify attribute type for: " + attribute);
                }
                
                if(dictionaryService != null) {
                    switch(attributeType) {
                    case PROPERTY:
                        PropertyDefinition propDef = dictionaryService.getProperty(attribute);
                        if(propDef == null)
                            throw new IllegalStateException("Unknown property: " + attribute);
                            
                        result.datatype = propDef.getDataType().getName();
                        result.nodetype = result.datatype.equals(DataTypeDefinition.CATEGORY)
                                ? ContentModel.TYPE_CATEGORY
                                : null;
                        break;
                    case ASSOCIATION:
                        AssociationDefinition assocDef = dictionaryService.getAssociation(attribute);
                        if(assocDef == null)
                            throw new IllegalStateException("Unknown association: " + attribute);
                        result.datatype = DataTypeDefinition.NODE_REF;
                        result.nodetype = assocDef.getTargetClass().getName();
                    }
                }
            }
            
            result.invariants = invariants != null ? new ArrayList<>(invariants) : null;
            
            return result;
        }
        
    }
    
}
