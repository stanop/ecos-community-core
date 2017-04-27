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
package ru.citeck.ecos.share.mvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.web.site.SlingshotPageViewResolver;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.surf.WebFrameworkServiceRegistry;
import org.springframework.extensions.surf.uri.UriTemplateListIndex;
import org.springframework.extensions.webscripts.UriTemplate;

/**
 * Page view resolver that sorts uri-templates.
 * 
 * @author Sergey Tiunov
 */
public class IDocsPageViewResolver extends SlingshotPageViewResolver 
{
	
	@Override
    public UriTemplateListIndex generateUriTemplateListIndexFromConfig(WebFrameworkServiceRegistry serviceRegistry, String targetElement)
    {
        List<UriTemplate> uriTemplates = null;
        
        Config config = serviceRegistry.getConfigService().getConfig("UriTemplate");
        if (config != null)
        {
            ConfigElement uriConfig = config.getConfigElement(targetElement);
            if (uriConfig != null)
            {
                List<ConfigElement> uriElements = uriConfig.getChildren("uri-template");
                if (uriElements != null)
                {
                    uriTemplates = new ArrayList<UriTemplate>(uriElements.size());
                    HashMap<String,UriTemplate> templatesByName = new HashMap<String, UriTemplate>(uriElements.size());

                    for (ConfigElement uriElement : uriElements)
                    {
                        String template = uriElement.getValue();
                        String id = uriElement.getAttribute("id");
                        if (template == null || template.trim().length() == 0)
                        {
                            throw new IllegalArgumentException("<uri-template> config element must contain a value.");
                        }

                        // build the object to represent the Uri Template
                        UriTemplate uriTemplate = new UriTemplate(template);
                        
                        templatesByName.put(id, uriTemplate);
                        
                        String before = uriElement.getAttribute("before");

                        // store the Uri Template
                        if(before == null) {
                        	uriTemplates.add(uriTemplate);
                        } else {
                        	UriTemplate beforeTemplate = templatesByName.get(before);
                        	int index = uriTemplates.indexOf(beforeTemplate);
                        	if(index != -1) {
                        		uriTemplates.add(index, uriTemplate);
                        	} else {
                        		uriTemplates.add(uriTemplate);
                        	}
                        }
                    }
                }
            }
        }
        return uriTemplates == null ? null : new UriTemplateListIndex(uriTemplates);
    }


}
