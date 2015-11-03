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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Class;
import org.alfresco.repo.dictionary.M2ClassAssociation;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.aop.MethodBeforeAdvice;

import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.utils.NamespacePrefixResolverMapImpl;

/**
 * DictionaryDAO.putModel* method interceptor.
 * Automatically adds association indexing properties to models.
 * 
 * @author Sergey Tiunov
 * 
 */
public class AddAssociationIndexProperties implements MethodBeforeAdvice {
	
	private Set<String> acceptedAuthors;
	private Set<String> acceptedAssocs;
	private AcceptCriteria acceptAll = new AcceptAll();
	private AcceptCriteria acceptSet;
	
    private AssociationIndexPropertyRegistry registry;

	public void init() {
		acceptSet = new AcceptSet(acceptedAssocs);
	}
	
	@Override
	public void before(Method method, Object[] args, Object target)
			throws Throwable {
		if(method.getName().startsWith("putModel") 
		&& args.length == 1 && args[0] instanceof M2Model) 
		{
			process((M2Model) args[0]);
		}
	}

	// process every class (type or aspect) in the model
	private void process(M2Model model) {
		
		AcceptCriteria acceptCriteria = null;
		
		// Processing our models only
		if (acceptedAuthors.contains(model.getAuthor())) {
			acceptCriteria = acceptAll;
		} else {
			acceptCriteria = acceptSet;
		}
		
        NamespacePrefixResolver nsPrefixResolver = new NamespacePrefixResolverMapImpl(getPrefixToUriMap(model));

		for(M2Class clazz : model.getTypes()) {
			process(clazz, acceptCriteria, nsPrefixResolver);
		}
		for(M2Class clazz : model.getAspects()) {
			process(clazz, acceptCriteria, nsPrefixResolver);
		}
	}
	
    private Map<String, String> getPrefixToUriMap(M2Model model) {
        Map<String, String> map = new HashMap<>();
        for(M2Namespace ns : model.getNamespaces()) {
            map.put(ns.getPrefix(), ns.getUri());
        }
        for(M2Namespace ns : model.getImports()) {
            map.put(ns.getPrefix(), ns.getUri());
        }
        return map;
    }

    // process every property in class (type or aspect)
	private void process(M2Class clazz, AcceptCriteria acceptCriteria, NamespacePrefixResolver nsPrefixResolver) {
		// find all index properties
		Set<String> definedProperties = new HashSet<String>();
		for(M2Property prop : clazz.getProperties()) {
			definedProperties.add(prop.getName());
		}

        for(M2ClassAssociation assoc : clazz.getAssociations()) {
            if(!acceptCriteria.isAccepted(assoc.getName())) 
                continue;
            
            // get index property name:
            String indexPropName = getAssocIndexPropertyName(assoc.getName(), nsPrefixResolver);
            
            // if it is made already - do not add it one more time
            if(definedProperties.contains(indexPropName)) continue;
            
            // <property name="*_INDEX">
            M2Property indexProp = clazz.createProperty(indexPropName);
                                                    // <property name="...">
            indexProp.setType("d:text");            //     <type>d:text</type>
            indexProp.setProtected(true);           //     <protected>true</protected>
            indexProp.setMultiValued(true);         //     <multiple>true</multiple>
            indexProp.setIndexed(true);             //     <index enabled="true">
            indexProp.setIndexedAtomically(true);   //         <atomic>true</atomic>
            indexProp.setStoredInIndex(false);      //         <stored>false</stored>
            indexProp.setIndexTokenisationMode(     //         <tokenised>false</tokenised>
                    IndexTokenisationMode.FALSE);   //     </index>
                                                    // </property>
        }
	}
	
    protected String getAssocIndexPropertyName(String assocName, NamespacePrefixResolver nsPrefixResolver) {
        QName assocQName = QName.createQName(assocName, nsPrefixResolver);
        QName indexQName = registry.getAssociationIndexProperty(assocQName);
        return indexQName.toPrefixString(nsPrefixResolver);
    }

	public void setAcceptedAuthors(Set<String> acceptedAuthors) {
		this.acceptedAuthors = acceptedAuthors;
	}

	public void setAcceptedAssocs(Set<String> acceptedAssocs) {
		this.acceptedAssocs = acceptedAssocs;
	}

    public void setRegistry(AssociationIndexPropertyRegistry registry) {
        this.registry = registry;
    }
    
	private interface AcceptCriteria {
		public boolean isAccepted(String name);
	}
	
	private class AcceptAll implements AcceptCriteria {
		public boolean isAccepted(String name) {
			return true;
		}
	}
	
	private class AcceptSet implements AcceptCriteria {
		private Set<String> set;
		public AcceptSet(Set<String> set) {
			this.set = set;
		}
		public boolean isAccepted(String name) {
			return set.contains(name);
		}
	}

}
