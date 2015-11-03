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
import java.util.Map;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Class;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * Sets index tokenisation mode to 'both' by default, so that ordering is supported in solr by default.
 * See https://tools.citeck.ru/redmine/issues/5351
 * 
 * @author Sergey Tiunov
 */
public class AddDefaultPropertiesTokenisation implements MethodBeforeAdvice {

    private Map<String, IndexTokenisationMode> modes;
    private IndexTokenisationMode defaultMode;
    
    @Override
    public void before(Method method, Object[] args, Object target)
            throws Throwable {
        if(method.getName().startsWith("putModel") 
        && args.length == 1 && args[0] instanceof M2Model) 
        {
            process((M2Model) args[0]);
        }
    }

    private void process(M2Model model) {
        for(M2Class clazz : model.getTypes()) {
            process(clazz);
        }
        for(M2Class clazz : model.getAspects()) {
            process(clazz);
        }
    }

    private void process(M2Class clazz) {
        for(M2Property property : clazz.getProperties()) {
            if(property.isIndexed() && property.getIndexTokenisationMode() == null) {
                if(modes.containsKey(property.getType())) {
                    property.setIndexTokenisationMode(modes.get(property.getType()));
                } else {
                    property.setIndexTokenisationMode(defaultMode);
                }
            }
        }
    }

    public void setModes(Map<String, IndexTokenisationMode> modes) {
        this.modes = modes;
    }

    public void setDefaultMode(IndexTokenisationMode defaultMode) {
        this.defaultMode = defaultMode;
    }

}
