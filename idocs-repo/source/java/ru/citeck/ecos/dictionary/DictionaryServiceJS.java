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

/**
 * This is lightweight interface to data dictionary.
 * It is brought from Share Data Dictionary.
 * See: 
 * class org.alfresco.web.scripts.DictionaryQuery (project slingshot);
 * http://wiki.alfresco.com/wiki/Share_Data_Dictionary
 * 
 * @author Sergey Tiunov
 */
public interface DictionaryServiceJS {
    
    /**
     * Get array of all type names.
     * 
     * @return
     */
    public String[] getAllTypes();
    
    /**
     * Get array of all aspect names.
     * 
     * @return
     */
    public String[] getAllAspects();
    
    /**
     * isSubType - return if the supplied type is a sub-type of a given type.
     * 
     * @param type      Type to test
     * @param isType    Is the type a subtype of this type?
     * 
     * @return true if the type is a subtype of isType or the same type
     */
    public boolean isSubType(final String type, final String isType);
    
    /**
     * hasDefaultAspect - return if the type definition has the default aspect applied..
     * 
     * @param type      Type to test
     * @param aspect    Aspect to look for in the default aspects
     * 
     * @return true if the aspect is one of the default aspects
     */
    public boolean hasDefaultAspect(final String type, final String aspect);
    
    /**
     * getDefaultAspects - return Array of default aspects for the given type.
     * 
     * @param type      Type to inspect
     * 
     * @return Array of default aspects for the type, can be empty but never null.
     */
    public String[] getDefaultAspects(final String type);
    
    /**
     * isAspect - return if the supplied dd class is an aspect.
     * 
     * @param ddclass   DD class to test
     * 
     * @return true if the supplied dd class is an aspect
     */
    public boolean isAspect(final String ddclass);
    
    /**
     * isType - return if the supplied dd class is a type.
     * 
     * @param ddclass   DD class to test
     * 
     * @return true if the supplied dd class is a type
     */
    public boolean isType(final String ddclass);
    
    /**
     * hasProperty - return if a type or aspect has the given property definition.
     * This method correctly reports properties inherited from base types.
     * 
     * @param ddclass   Type or aspect to test
     * @param property  Property to look for in the type or aspect definition
     * 
     * @return true if the property is defined on the type or aspect
     */
    public boolean hasProperty(final String ddclass, final String property);
    
    /**
     * hasProperty - return if a type or aspect has the given property definition.
     * This method correctly reports properties inherited from base types and also
     * optionally checks any default aspects applied to the type for the property.
     * 
     * @param ddclass   Type or aspect to test
     * @param property  Property to look for in the type or aspect definition
     * @param includeDefaultAspects If true, check default aspects for the given property.
     * 
     * @return true if the property is defined on the type or aspect or any of its default aspects.
     */
    public boolean hasProperty(final String ddclass, final String property, final boolean includeDefaultAspects);

    /**
     * getTitle - return the title string for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * 
     * @return title string
     */
    public String getTitle(final String ddclass);
    
    /**
     * getDescription - return the description string for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * 
     * @return description string
     */
    public String getDescription(final String ddclass);
    
    /**
     * getParent - return the parent for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * 
     * @return parent type or null for a root type with no parent.
     */
    public String getParent(final String ddclass);
    
    /**
     * isContainer - return if the specified type is a container
     * 
     * @param type      Type to inspect
     * 
     * @return true if the type is a container, false otherwise
     */
    public boolean isContainer(final String type);
    
    /**
     * getProperty - return a single named property for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * @param property  Property to look for in the type or aspect definition
     * 
     * @return DictionaryProperty describing the property definition or null if not found
     */
    public DictionaryProperty getProperty(final String ddclass, final String property);
    
    /**
     * getProperty - return a single named property for the given dd class, optionally
     * retrieve a property from the default aspects.
     * 
     * @param ddclass   DD class to inspect
     * @param property  Property to look for in the type or aspect definition
     * @param includeDefaultAspects If true, check default aspects for the given property.
     * 
     * @return DictionaryProperty describing the property definition or null if not found
     */
    public DictionaryProperty getProperty(final String ddclass, final String property, final boolean includeDefaultAspects);
    
    /**
     * getProperties - return all properties for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * 
     * @return Array of DictionaryProperty objects describing the property definitions for the class.
     *         Can be empty but never null.
     */
    public DictionaryProperty[] getProperties(final String ddclass);
    
    /**
     * getProperties - return all properties for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * @param includeDefaultAspects If true, also retrieve properties from the default aspects.
     * 
     * @return Array of DictionaryProperty objects describing the property definitions for the class
     *         and default aspects. Can be empty but never null.
     */
    public DictionaryProperty[] getProperties(final String ddclass, final boolean includeDefaultAspects);
    
    /**
     * getAssociations - return the target associations for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * 
     * @return Array of DictionaryAssoc objects describing the target associations for the class.
     *         Can be empty but never null.
     */
    public DictionaryAssociation[] getAssociations(final String ddclass);
    
    /**
     * getChildAssociations - return the child associations for the given dd class.
     * 
     * @param ddclass   DD class to inspect
     * 
     * @return Array of DictionaryAssoc objects describing the child associations for the class.
     *         Can be empty but never null.
     */
    public DictionaryAssociation[] getChildAssociations(final String ddclass);
    
    public interface DictionaryProperty {
        public String getName();
        public String getTitle();
        public String getDescription();
        public String getDataType();
        public String getDefaultValue();
        public boolean getIsMultiValued();
        public boolean getIsMandatory();
        public boolean getIsEnforced();
        public boolean getIsProtected();
        public boolean getIsIndexed();
    }

    public interface DictionaryAssociation {
        public String getName();
        public String getTitle();
        public String getSourceClass();
        public String getSourceRole();
        public boolean getSourceIsMandatory();
        public boolean getSourceIsMany();
        public String getTargetClass();
        public String getTargetRole();
        public boolean getTargetIsMandatory();
        public boolean getTargetIsMany();
    }

}
