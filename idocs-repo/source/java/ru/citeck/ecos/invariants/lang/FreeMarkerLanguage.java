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
package ru.citeck.ecos.invariants.lang;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TemplateService;

import ru.citeck.ecos.invariants.AbstractInvariantLanguage;
import ru.citeck.ecos.utils.RepoUtils;

public class FreeMarkerLanguage extends AbstractInvariantLanguage {

    private static final String NAME = "freemarker";
    private TemplateService templateService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isValueSupported(Object value) {
        return value instanceof String;
    }
    
    @Override
    public Object evaluate(Object expressionObj, Map<String, Object> model) {
        String expression = (String) expressionObj;
        Map<String, Object> innerModel = new HashMap<>();
        innerModel.putAll(RepoUtils.buildDefaultModel(serviceRegistry));
        innerModel.putAll(model);
        try {
            return templateService.processTemplateString("freemarker", expression, innerModel);
        } catch(RuntimeException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return NAME + " invariant language";
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

}
