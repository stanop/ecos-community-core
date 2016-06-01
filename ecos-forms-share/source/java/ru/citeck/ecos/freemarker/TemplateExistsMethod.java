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
package ru.citeck.ecos.freemarker;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class TemplateExistsMethod implements TemplateMethodModel {

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
        if(args.size() < 1) throw new IllegalArgumentException("path should be specified for template_exists method");
        String path = (String) args.get(0);
        
        Environment environment = Environment.getCurrentEnvironment();
        
        // get the current template's parent directory to use when searching for relative paths
        String currentTemplateName = environment.getTemplate().getName();
        String currentTemplateDir = FilenameUtils.getPath(currentTemplateName);
        
        // look up the path relative to the current working directory (this also works for absolute paths)
        String fullTemplatePath = TemplateCache.getFullTemplatePath(environment, currentTemplateDir, path);
        
        TemplateLoader templateLoader = environment.getConfiguration().getTemplateLoader();
        boolean exists;
        try {
            exists = templateLoader.findTemplateSource(fullTemplatePath) != null;
        } catch (IOException e) {
            exists = false;
        }
        
        return exists ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
    }

}
