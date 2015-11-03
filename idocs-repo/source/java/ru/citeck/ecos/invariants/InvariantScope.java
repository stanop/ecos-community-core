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

import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class InvariantScope {

    public enum ClassScopeKind {
        TYPE("t"),
        ASPECT("a");
        
        private final String prefix;
        private ClassScopeKind(String prefix) {
            this.prefix = prefix;
        }
        
        private String getPrefix() {
            return prefix;
        }
    }
    
    public enum AttributeScopeKind {
        PROPERTY("p", "property", true),
        ASSOCIATION("a", "association", true),
        CHILD_ASSOCIATION("c", "child-association", true),
        PROPERTY_TYPE("P", "property_type", false),
        ASSOCIATION_TYPE("A", "association_type", false),
        CHILD_ASSOCIATION_TYPE("C", "child-association_type", false);
        
        private final String prefix;
        private final String string;
        private final boolean concrete;
        private AttributeScopeKind(String prefix, String string, boolean concrete) {
            this.prefix = prefix;
            this.string = string;
            this.concrete = concrete;
        }
        
        private String getPrefix() {
            return prefix;
        }
        
        public boolean isConcrete() {
            return concrete;
        }
        
        @Override
        public String toString() {
            return string;
        }
    }

    // scope
    private QName classScope;
    private ClassScopeKind classScopeKind;
    private QName attributeScope;
    private AttributeScopeKind attributeScopeKind;
    
    public InvariantScope(QName classScope) {
        this.classScope = classScope;
    }
    
    public InvariantScope(QName classScope, ClassScopeKind kind) {
        this.classScope = classScope;
        this.classScopeKind = kind;
    }
    
    public InvariantScope(QName attributeScope, AttributeScopeKind kind) {
        this.attributeScope = attributeScope;
        this.attributeScopeKind = kind;
    }
    
    public InvariantScope(QName classScope, QName attributeScope, AttributeScopeKind kind) {
        this.classScope = classScope;
        this.attributeScope = attributeScope;
        this.attributeScopeKind = kind;
    }
    
    public InvariantScope(QName classScope, ClassScopeKind classScopeKind, QName attributeScope, AttributeScopeKind attributeScopeKind) {
        this.classScope = classScope;
        this.classScopeKind = classScopeKind;
        this.attributeScope = attributeScope;
        this.attributeScopeKind = attributeScopeKind;
    }
    
    public InvariantScope(AttributeScopeKind attributeScopeKind) {
        this.attributeScopeKind = attributeScopeKind;
    }
    
    public QName getClassScope() {
        return classScope;
    }

    public ClassScopeKind getClassScopeKind() {
        return classScopeKind;
    }

    public QName getAttributeScope() {
        return attributeScope;
    }

    public AttributeScopeKind getAttributeScopeKind() {
        return attributeScopeKind;
    }
    
    public boolean matches(InvariantScope that) {
        if(that == null) return false;
        boolean attributeMatches = 
                this.attributeScope == null || 
                that.attributeScope == null || 
                this.attributeScope.equals(that.attributeScope) && 
                this.attributeScopeKind == that.attributeScopeKind;
        if(!attributeMatches) return false;
        
        boolean classMatches = 
                this.classScope == null || 
                that.classScope == null || 
                this.classScope.equals(that.classScope);
        if(!classMatches) return false;
        
        return true;
    }
    
    @Override
    public boolean equals(Object object) {
        if(object instanceof InvariantScope) {
            InvariantScope that = (InvariantScope) object;
            return ObjectUtils.equals(this.classScope, that.classScope) 
                && ObjectUtils.equals(this.attributeScope, that.attributeScope)
                && ObjectUtils.equals(this.attributeScopeKind, that.attributeScopeKind);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(classScope)
                .append(attributeScope)
                .append(attributeScopeKind)
                .toHashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(classScope != null) {
            if(classScopeKind != null) {
                sb.append(classScopeKind.getPrefix()).append(":");
            }
            sb.append(classScope);
        } else {
            sb.append("*");
        }
        sb.append(".");
        if(attributeScope != null) {
            if(attributeScopeKind != null) {
                sb.append(attributeScopeKind.getPrefix()).append(":");
            }
            sb.append(attributeScope);
        } else {
            sb.append("*");
        }
        return sb.toString();
    }
    
}
