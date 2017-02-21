/*
 * Copyright (C) 2008-2017 Citeck LLC.
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
package ru.citeck.ecos.repo.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.util.PropertyCheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.DefaultResourceLoader;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.repo.jscript.ClasspathScriptLocation;

public class ScriptImporterModuleComponent extends AbstractModuleComponent
{
	private static final Log logger = LogFactory.getLog(ScriptImporterModuleComponent.class);
    private String bootstrapScript;
    private ScriptService scriptService;
    private String scriptEngine;

    /**
     * Set a bootstrap script to import.<br/>
     * 
     * @param bootstrapScript the bootstrap data location
     */
    public void setBootstrapScript(String bootstrapScript)
    {
        this.bootstrapScript = bootstrapScript;
    }

    @Override
    protected void checkProperties()
    {
        if (bootstrapScript == null)
        {
            PropertyCheck.mandatory(this, null, "bootstrapScript");
        }
        super.checkProperties();
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setScriptEngine(String engine) {
        this.scriptEngine = engine;
    }

    @Override
    protected void executeInternal() throws Throwable
    {
        Map<String, Object> model = new HashMap<String, Object>();
        ClasspathScriptLocation location = new ClasspathScriptLocation(bootstrapScript);
        Object result = scriptService.executeScript((ScriptLocation)location, model);
        // Done
    }
}
