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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.invariants.InvariantScope.ClassScopeKind;
import ru.citeck.ecos.invariants.xml.Aspect;
import ru.citeck.ecos.invariants.xml.Association;
import ru.citeck.ecos.invariants.xml.Associations;
import ru.citeck.ecos.invariants.xml.AttributeScope;
import ru.citeck.ecos.invariants.xml.AttributesScope;
import ru.citeck.ecos.invariants.xml.ChildAssociation;
import ru.citeck.ecos.invariants.xml.ChildAssociations;
import ru.citeck.ecos.invariants.xml.ClassScope;
import ru.citeck.ecos.invariants.xml.Criterion;
import ru.citeck.ecos.invariants.xml.Invariant;
import ru.citeck.ecos.invariants.xml.Property;
import ru.citeck.ecos.invariants.xml.Properties;
import ru.citeck.ecos.invariants.xml.Scope;
import ru.citeck.ecos.invariants.xml.Type;
import ru.citeck.ecos.search.SearchCriteria;

public class InvariantDefinition {
    
    private InvariantScope scope;
    private Feature feature;
    private InvariantPriority priority;
    private String description;
    private String language;
    private String expression;
    private Object explicitValue;
    private boolean isFinal = false;
    
    private InvariantDefinition(InvariantScope scope, Feature feature, String language) {
        this.scope = scope;
        this.feature = feature;
        this.language = language;
    }

    public Feature getFeature() {
        return feature;
    }
    
    public InvariantPriority getPriority() {
        return priority;
    }

    public String getDescription() {
        return description != null ? description : feature.getDefaultDescription();
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public Object getValue() {
        return explicitValue;
    }
    
    private void setValue(String value) {
        explicitValue = value;
    }
    
    private void setValue(List<?> list) {
        explicitValue = list;
    }
    
    private void setValue(SearchCriteria criteria) {
        explicitValue = criteria;
    }
    
    public boolean isFinal() {
        return this.isFinal;
    }
    
    private void makeFinal() {
        this.isFinal  = true;
    }
    
    public InvariantScope getScope() {
        return scope;
    }
    
    public QName getClassScope() {
        return scope.getClassScope();
    }

    public ClassScopeKind getClassScopeKind() {
        return scope.getClassScopeKind();
    }

    public QName getAttributeScope() {
        return scope.getAttributeScope();
    }

    public AttributeScopeKind getAttributeScopeKind() {
        return scope.getAttributeScopeKind();
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append(priority)
                .append(":")
                .append(scope)
                .append(".")
                .append(feature.toString().toLowerCase())
                .append("=")
                .append("[")
                .append(language)
                .append("]")
                .append(expression != null ? expression : explicitValue)
                .toString();
    }
    
    public static class Builder {
        
        private NamespacePrefixResolver prefixResolver;
        
        // scope
        private QName classScope;
        private ClassScopeKind classScopeKind;
        private QName attributeScope;
        private AttributeScopeKind attributeScopeKind;
        
        private Feature feature;
        private InvariantPriority priority = InvariantPriority.MODULE;
        private String description;
        private String language;
        private String expression;
        private Object value;
        private boolean isFinal;
        
        public Builder(NamespacePrefixResolver prefixResolver) {
            this.prefixResolver = prefixResolver;
        }
        
        private Builder pushScope(ClassScope scope, ClassScopeKind kind) {
            if(classScopeKind != null) {
                throw new IllegalStateException("Class scope is already set");
            }
            classScope = QName.createQName(scope.getName(), prefixResolver);
            classScopeKind = kind;
            return this;
        }
        
        /*package*/ Builder pushScope(Object scope) {
            if(scope instanceof Type) {
                return pushScope((Type) scope);
            }
            if(scope instanceof Aspect) {
                return pushScope((Aspect) scope);
            }
            if(scope instanceof Property) {
                return pushScope((Property) scope);
            }
            if(scope instanceof Association) {
                return pushScope((Association) scope);
            }
            if(scope instanceof ChildAssociation) {
                return pushScope((ChildAssociation) scope);
            }
            if(scope instanceof Properties) {
                return pushScope((Properties) scope);
            }
            if(scope instanceof Associations) {
                return pushScope((Associations) scope);
            }
            if(scope instanceof ChildAssociations) {
                return pushScope((ChildAssociations) scope);
            }
            throw new IllegalArgumentException("Unsupported scope type: " + scope);
        }
        
        /*package*/ Builder popScope(Object scope) {
            if(scope instanceof ClassScope) {
                return popScope((ClassScope) scope);
            }
            if(scope instanceof AttributeScope) {
                return popScope((AttributeScope) scope);
            }
            if(scope instanceof AttributesScope) {
                return popScope((AttributesScope) scope);
            }
            throw new IllegalArgumentException("Unsupported scope type: " + scope);
        }
        
        /*package*/ Builder pushScope(Type scope) {
            return pushScope(scope, ClassScopeKind.TYPE);
        }
        
        /*package*/ Builder pushScope(Aspect scope) {
            return pushScope(scope, ClassScopeKind.ASPECT);
        }
        
        /*package*/ Builder popScope(ClassScope scope) {
            classScope = null;
            classScopeKind = null;
            return this;
        }
        
        /*package*/ Builder pushScope(AttributeScope scope, AttributeScopeKind kind) {
            if(attributeScopeKind != null) {
                throw new IllegalStateException("Attribute scope is already set");
            }
            attributeScope = QName.createQName(scope.getName(), prefixResolver);
            attributeScopeKind = kind;
            return this;
        }
        
        /*package*/ Builder pushScope(Property scope) {
            return pushScope(scope, AttributeScopeKind.PROPERTY);
        }
        
        /*package*/ Builder pushScope(Association scope) {
            return pushScope(scope, AttributeScopeKind.ASSOCIATION);
        }
        
        /*package*/ Builder pushScope(ChildAssociation scope) {
            return pushScope(scope, AttributeScopeKind.CHILD_ASSOCIATION);
        }
        
        /*package*/ Builder popScope(AttributeScope scope) {
            attributeScope = null;
            attributeScopeKind = null;
            return this;
        }
        
        /*package*/ Builder pushScope(AttributesScope scope, AttributeScopeKind kind) {
            if(attributeScopeKind != null) {
                throw new IllegalStateException("Attribute scope is already set");
            }
            attributeScope = QName.createQName(scope.getType(), prefixResolver);
            attributeScopeKind = kind;
            return this;
        }
        
        /*package*/ Builder pushScope(Properties scope) {
            return pushScope(scope, AttributeScopeKind.PROPERTY_TYPE);
        }
        
        /*package*/ Builder pushScope(Associations scope) {
            return pushScope(scope, AttributeScopeKind.ASSOCIATION_TYPE);
        }
        
        /*package*/ Builder pushScope(ChildAssociations scope) {
            return pushScope(scope, AttributeScopeKind.CHILD_ASSOCIATION_TYPE);
        }
        
        /*package*/ Builder popScope(AttributesScope scope) {
            attributeScope = null;
            attributeScopeKind = null;
            return this;
        }
        
        public Builder pushScope(ClassDefinition scope) {
            classScope = scope.getName();
            classScopeKind = scope.isAspect() ? ClassScopeKind.ASPECT : ClassScopeKind.TYPE;
            return this;
        }
        
        public Builder pushScope(PropertyDefinition scope) {
            attributeScope = scope.getName();
            attributeScopeKind = AttributeScopeKind.PROPERTY;
            return this;
        }
        
        public Builder pushScope(AssociationDefinition scope) {
            attributeScope = scope.getName();
            attributeScopeKind = scope.isChild() 
                    ? AttributeScopeKind.CHILD_ASSOCIATION 
                    : AttributeScopeKind.ASSOCIATION;
            return this;
        }
        
        public Builder popScope(ClassDefinition scope) {
            classScope = null;
            classScopeKind = null;
            return this;
        }
        
        public Builder popScope(PropertyDefinition scope) {
            attributeScope = null;
            attributeScopeKind = null;
            return this;
        }
        
        public Builder popScope(AssociationDefinition scope) {
            attributeScope = null;
            attributeScopeKind = null;
            return this;
        }
        
        public Builder pushScope(QName scope, ClassScopeKind kind) {
            classScope = scope;
            classScopeKind = kind;
            return this;
        }
        
        public Builder pushScope(QName scope, AttributeScopeKind kind) {
            attributeScope = scope;
            attributeScopeKind = kind;
            return this;
        }
        
        public Builder popScope(QName scope, ClassScopeKind kind) {
            classScope = null;
            classScopeKind = null;
            return this;
        }
        
        public Builder popScope(QName scope, AttributeScopeKind kind) {
            attributeScope = null;
            attributeScopeKind = null;
            return this;
        }
        
        /*package*/ Builder data(Invariant data) {
            feature = Feature.valueOf(data.getOn());
            description = data.getDescription();
            language = data.getLanguage().value();
            isFinal = data.isFinal() != null ? data.isFinal() : false;
            
            List<Serializable> contents = filterContent(data.getContent());
            if(contents.size() > 0) {
                Serializable first = contents.get(0);
                if(first instanceof String) {
                    if(contents.size() > 1) {
                        throw new IllegalArgumentException("Invariant should contain either text, or nested elements, but not both");
                    }
                    expression = (String) first;
                    value = first;
                } else if(first instanceof JAXBElement) {
                    
                    Object firstElement = ((JAXBElement<?>) first).getValue();
                    if(firstElement instanceof String) {
                        
                        List<String> itemList = getElementList(contents, String.class);
                        value = itemList;
                        
                    } else if(firstElement instanceof Criterion) {
                        
                        List<Criterion> criterionList = getElementList(contents, Criterion.class);
                        SearchCriteria criteria = new SearchCriteria(prefixResolver);
                        for(Criterion criterion : criterionList) {
                            criteria.addCriteriaTriplet(criterion.getAttribute(), criterion.getPredicate(), criterion.getValue());
                        }
                        value = criteria;
                        
                    } else {
                        throw new IllegalStateException("Invariant element is neither String, nor Criterion, but " + 
                                (firstElement != null ? firstElement.getClass() : null));
                    }
                    
                } else {
                    throw new IllegalStateException("Invariant contents is neither String, nor JAXBElement, but " + 
                            (first != null ? first.getClass() : null));
                }
            }
            return this;
        }
        
        public Builder feature(Feature feature) {
            this.feature = feature;
            return this;
        }
        
        public Builder feature(String feature) {
            this.feature = Feature.valueOf(feature);
            return this;
        }
        
        public Builder priority(InvariantPriority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder priority(String priority) {
            this.priority = InvariantPriority.valueOf(priority);
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder lang(String language) {
            return this.language(language);
        }
        
        public Builder language(String language) {
            this.language = language;
            return this;
        }
        
        public Builder expression(String expression) {
            this.expression = expression;
            this.value = expression;
            return this;
        }
        
        public Builder value(Object value) {
            this.value = value;
            return this;
        }
        
        public Builder explicit(Object value) {
            this.language = InvariantConstants.LANGUAGE_EXPLICIT;
            this.value = value;
            return this;
        }
        
        public Builder criteria(SearchCriteria criteria) {
            this.language = InvariantConstants.LANGUAGE_CRITERIA;
            this.value = criteria;
            return this;
        }
        
        public InvariantDefinition build() {
            check(attributeScopeKind != null, "invariant scope should be defined");
            check(feature != null, "invariant feature should be defined");
            check(language != null, "invariant language should be defined");
            check(value != null, "invariant value should be defined");
            
            // TODO check language vs value
            
            // TODO check feature vs value (if not javascript)
            
            InvariantScope scope = new InvariantScope(classScope, classScopeKind, attributeScope, attributeScopeKind);
            InvariantDefinition invariant = new InvariantDefinition(scope, feature, language);
            invariant.priority = priority;
            invariant.description = description;
            invariant.expression = expression;
            
            if(value instanceof List) {
                invariant.setValue((List<?>) value);
            } else if(value instanceof SearchCriteria) {
                invariant.setValue((SearchCriteria) value);
            } else if(value != null) {
                invariant.setValue(value.toString());
            }
            
            if(isFinal) {
                invariant.makeFinal();
            }
            
            return invariant;
        }
        
        public InvariantDefinition buildFinal() {
            InvariantDefinition definition = build();
            definition.makeFinal();
            return definition;
        }
        
        private void check(boolean assertion, String message) {
            if(!assertion) {
                throw new IllegalStateException(message);
            }
        }
        
        private List<Serializable> filterContent(List<Serializable> content) {
            List<Serializable> filtered = new ArrayList<Serializable>(content.size());
            for(Serializable element : content) {
                if(element instanceof String) {
                    String string = (String) element;
                    string = string.trim();
                    if(string.isEmpty()) {
                        continue;
                    }
                }
                filtered.add(element);
            }
            return filtered;
        }

        @SuppressWarnings("unchecked")
        private <T> List<T> getElementList(List<Serializable> contents, Class<T> elementClass) {
            List<T> elementList = new ArrayList<T>(contents.size());
            for(Serializable content : contents) {
                if(!(content instanceof JAXBElement)) {
                    throw new IllegalStateException("Expected JAXBElement, but got " + 
                            (content != null ? content.getClass() : null));
                }
                JAXBElement<?> element = (JAXBElement<?>) content;
                Object value = element.getValue();
                if(!elementClass.isInstance(value)) {
                    throw new IllegalStateException("Expected " + elementClass + ", but got " + 
                            (value != null ? value.getClass() : null));
                }
                elementList.add((T) value);
            }
            return elementList;
        }

        public static List<InvariantDefinition> buildInvariants(List<Scope> scopes, InvariantPriority priority, NamespacePrefixResolver prefixResolver) {
            Builder builder = new Builder(prefixResolver)
                    .priority(priority);
            List<InvariantDefinition> invariants = new LinkedList<>();
            
            for(Scope scope : scopes) {
                processScope(builder, scope, invariants);
            }
            
            return invariants;
        }
        
        private static void processScope(Builder builder, Scope scope, List<InvariantDefinition> invariants) {
            builder.pushScope(scope);
            for(Object object : scope.getScoped()) {
                if(object instanceof Scope) {
                    processScope(builder, (Scope) object, invariants);
                } else if(object instanceof Invariant) {
                    invariants.add(builder.data((Invariant) object).build());
                } else {
                    throw new IllegalStateException("Scope can contain only child scopes and invariants");
                }
            }
            builder.popScope(scope);
        }

        public static List<InvariantDefinition> buildInvariants(QName attributeScope, 
                AttributeScopeKind attributeScopeKind, InvariantPriority priority, 
                List<Invariant> data, NamespacePrefixResolver prefixResolver) {
            if(attributeScope == null || attributeScopeKind == null) {
                return Collections.emptyList();
            }
            Builder builder = new Builder(prefixResolver)
                    .pushScope(attributeScope, attributeScopeKind)
                    .priority(priority);
            List<InvariantDefinition> invariants = new ArrayList<>(data.size());
            for(Invariant datum : data) {
                invariants.add(builder.data(datum).build());
            }
            return invariants;
        }
        
    }

}
