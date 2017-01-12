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
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Class;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.springframework.aop.MethodBeforeAdvice;

import ru.citeck.ecos.model.CiteckNumericModel;

/**
 * DictionaryDAO.putModel* method interceptor.
 * Automatically adds big-number indexing properties to models.
 * 
 * @author Sergey Tiunov
 *
 */
public class AddBigNumberIndexProperties implements MethodBeforeAdvice {

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
		for(M2Class clazz : model.getTypes()) {
			process(clazz);
		}
		for(M2Class clazz : model.getAspects()) {
			process(clazz);
		}
	}
	
	// process every property in class (type or aspect)
	private void process(M2Class clazz) {
		// find all big-number properties and index properties
		Set<String> bigProps = new HashSet<String>();
		Set<String> indexProps = new HashSet<String>();
		for(M2Property property : clazz.getProperties()) {
			if(CiteckNumericModel.isBigProp(property.getName())) {
				bigProps.add(property.getName());
			}
			if(CiteckNumericModel.isIndexProp(property.getName())) {
				indexProps.add(property.getName());
			}
		}

		// for each big property add index property:
		for(String bigProp : bigProps) {
			// get index property name:
			String indexPropName = CiteckNumericModel.getIndexProp(bigProp);
			// if it is made already - do not add it one more time
			if(indexProps.contains(indexPropName)) continue;
			
			// <property name="*_INDEX">
			M2Property indexProp = clazz.createProperty(indexPropName);
			// <type>d:text</type>
			indexProp.setType("d:text");
			// <protected>true</protected>
			indexProp.setProtected(true);
			// <index enabled="true">
			indexProp.setIndexed(true);
			// <atomic>true</atomic>
			indexProp.setIndexedAtomically(true);
			// <stored>false</stored>
			indexProp.setStoredInIndex(false);
			// <tokenised>false</tokenised>
			indexProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
		}
	}

}
