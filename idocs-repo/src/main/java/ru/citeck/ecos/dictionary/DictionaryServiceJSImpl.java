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
package ru.citeck.ecos.dictionary;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

class DictionaryServiceJSImpl extends AlfrescoScopableProcessorExtension implements DictionaryServiceJS {
    
    private NamespaceService namespaceService;
    private NamespaceService getNamespaceService() {
        if(namespaceService == null) {
            namespaceService = serviceRegistry.getNamespaceService();
        }
        return namespaceService;
    }

    private DictionaryService dictionaryService;
    private DictionaryService getDictionaryService() {
        if(dictionaryService == null) {
            dictionaryService = serviceRegistry.getDictionaryService();
        }
        return dictionaryService;
    }

    @Override
    public String[] getAllTypes() {
        return convert(dictionaryService.getAllTypes());
    }

    @Override
    public String[] getAllAspects() {
        return convert(dictionaryService.getAllAspects());
    }

    @Override
    public boolean isSubType(String type, String isType) {
        return getDictionaryService().isSubClass(convert(type), convert(isType));
    }

    @Override
    public boolean hasDefaultAspect(String type, String aspect) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(type));
        Set<QName> defaultAspects = classDef.getDefaultAspectNames();
        return defaultAspects.contains(convert(aspect));
    }

    @Override
    public String[] getDefaultAspects(String type) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(type));
        Set<QName> defaultAspects = classDef.getDefaultAspectNames();
        return convert(defaultAspects);
    }

    @Override
    public boolean isAspect(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return classDef.isAspect();
    }

    @Override
    public boolean isType(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return !classDef.isAspect();
    }

    @Override
    public boolean hasProperty(String ddclass, String property) {
        return hasProperty(ddclass, property, false);
    }

    @Override
    public boolean hasProperty(String ddclass, String property, boolean includeDefaultAspects) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        QName propQName = convert(property);
        if(getProperty(classDef, propQName) != null) 
            return true;
        if(!includeDefaultAspects) return false;
        for(ClassDefinition aspectDef : classDef.getDefaultAspects()) {
            if(getProperty(aspectDef, propQName) != null) 
                return true;
        }
        return false;
    }

    @Override
    public String getTitle(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return classDef.getTitle();
    }

    @Override
    public String getDescription(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return classDef.getDescription();
    }

    @Override
    public String getParent(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return convert(classDef.getParentName());
    }

    @Override
    public boolean isContainer(String type) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(type));
        return classDef.isContainer();
    }

    @Override
    public DictionaryProperty getProperty(String ddclass, String property) {
        return getProperty(ddclass, property, false);
    }

    @Override
    public DictionaryProperty getProperty(String ddclass, String property, boolean includeDefaultAspects) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        QName propQName = convert(property);
        PropertyDefinition propDef = getProperty(classDef, propQName);
        if(propDef != null) 
            return convert(propDef);
        if(!includeDefaultAspects) return null;
        for(ClassDefinition aspectDef : classDef.getDefaultAspects()) {
            propDef = getProperty(aspectDef, propQName);
            if(propDef != null) 
                return convert(propDef);
        }
        return null;
    }

    @Override
    public DictionaryProperty[] getProperties(String ddclass) {
        return getProperties(ddclass, false);
    }

    @Override
    public DictionaryProperty[] getProperties(String ddclass, boolean includeDefaultAspects) {
        List<PropertyDefinition> propDefs = new LinkedList<>();
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        propDefs.addAll(classDef.getProperties().values());
        if(includeDefaultAspects) {
            for(ClassDefinition aspectDef : classDef.getDefaultAspects()) {
                propDefs.addAll(aspectDef.getProperties().values());
            }
        }
        return convertProps(propDefs);
    }

    @Override
    public DictionaryAssociation[] getAssociations(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return convertAssocs(classDef.getAssociations().values());
    }

    @Override
    public DictionaryAssociation[] getChildAssociations(String ddclass) {
        ClassDefinition classDef = getDictionaryService().getClass(convert(ddclass));
        return convertAssocs(classDef.getChildAssociations().values());
    }
    
    private String convert(QName qname) {
        if(qname == null) return null;
        return qname.toPrefixString(getNamespaceService());
    }

    private QName convert(String string) {
        if(string == null) return null;
        return QName.resolveToQName(getNamespaceService(), string);
    }

    private String[] convert(Collection<QName> qnames) {
        String[] strings = new String[qnames.size()];
        int i = 0;
        for(QName qname : qnames) {
            strings[i++] = convert(qname);
        }
        return strings;
    }

    private PropertyDefinition getProperty(ClassDefinition classDef, QName property) {
        Map<QName, PropertyDefinition> propDefs = classDef.getProperties();
        return propDefs.get(property);
    }

    private DictionaryProperty convert(PropertyDefinition property) {
        return new DictionaryPropertyImpl(property);
    }

    private DictionaryProperty[] convertProps(Collection<PropertyDefinition> propDefs) {
        DictionaryProperty[] props = new DictionaryProperty[propDefs.size()];
        int i = 0;
        for(PropertyDefinition propDef : propDefs) {
            props[i++] = convert(propDef);
        }
        return props;
    }

    private DictionaryAssociation convert(AssociationDefinition association) {
        return new DictionaryAssociationImpl(association);
    }

    private DictionaryAssociation[] convertAssocs(Collection<? extends AssociationDefinition> assocDefs) {
        DictionaryAssociation[] assocs = new DictionaryAssociation[assocDefs.size()];
        int i = 0;
        for(AssociationDefinition assocDef : assocDefs) {
            assocs[i++] = convert(assocDef);
        }
        return assocs;
    }
    
    private final class DictionaryPropertyImpl implements DictionaryProperty {
        
        private PropertyDefinition impl;
        
        public DictionaryPropertyImpl(PropertyDefinition impl) {
            this.impl = impl;
        }
        
        @Override public String getName()           { return convert(impl.getName()); }
        @Override public String getTitle()          { return impl.getTitle(); }
        @Override public String getDescription()    { return impl.getDescription(); }
        @Override public String getDataType()       { return convert(impl.getDataType().getName()); }
        @Override public String getDefaultValue()   { return impl.getDefaultValue(); }
        @Override public boolean getIsMultiValued() { return impl.isMultiValued(); }
        @Override public boolean getIsMandatory()   { return impl.isMandatory(); }
        @Override public boolean getIsEnforced()    { return impl.isMandatoryEnforced(); }
        @Override public boolean getIsProtected()   { return impl.isProtected(); }
        @Override public boolean getIsIndexed()     { return impl.isIndexed(); }
        
    }
        
    private final class DictionaryAssociationImpl implements
            DictionaryAssociation {
        
        private AssociationDefinition impl;
        
        public DictionaryAssociationImpl(AssociationDefinition impl) {
            this.impl = impl;
        }
        
        @Override public String getName()           { return convert(impl.getName()); }
        @Override public String getTitle()              { return impl.getTitle(); }
        @Override public String getSourceClass()        { return convert(impl.getSourceClass().getName()); }
        @Override public String getSourceRole()         { return convert(impl.getSourceRoleName()); }
        @Override public boolean getSourceIsMandatory() { return impl.isSourceMandatory(); }
        @Override public boolean getSourceIsMany()      { return impl.isSourceMany(); }
        @Override public String getTargetClass()        { return convert(impl.getTargetClass().getName()); }
        @Override public String getTargetRole()         { return convert(impl.getTargetRoleName()); }
        @Override public boolean getTargetIsMandatory() { return impl.isTargetMandatory(); }
        @Override public boolean getTargetIsMany()      { return impl.isTargetMany(); }
        
    }

}
