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
package ru.citeck.ecos.webscripts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.alfresco.web.config.forms.FormsConfigElement;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

public class DependenciesGet extends AbstractWebScript
{
	
	private static final String PARAM_FAMILY = "family";
	private static final String PARAM_SUFFIX = "suffix";

	private static final Object FAMILY_FORMS = "forms";
	
	private static final String PARAM_DEPENDENCIES = "dependencies";
	private static final String PARAM_SRC = "src";

	private ConfigService configService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
		throws IOException
	{
		// get webscript parameters: family and format
		// e.g. DocLibCustom.js - family=DocLibCustom, format=js
		Map<String,String> templateArgs = req.getServiceMatch().getTemplateVars();
		String family = templateArgs.get(PARAM_FAMILY);
		String format = req.getFormat();
		String suffix = req.getParameter(PARAM_SUFFIX);

		// get dependencies for specified family and format
		List<String> dependencies = new ArrayList<String>();
		// forms have special treating
		if(family.equals(FAMILY_FORMS)) 
		{
			FormsConfigElement formsConfig = (FormsConfigElement)configService.getGlobalConfig().getConfigElement("forms");
			if(format.equals("js")) {
				dependencies.addAll(Arrays.asList(formsConfig.getDependencies().getJs()));
			} else if(format.equals("css")) {
				dependencies.addAll(Arrays.asList(formsConfig.getDependencies().getCss()));
			}
		} 
		// general case
		else 
		{
			ConfigElement dependencyConfig = configService.getConfig(family).getConfigElement(PARAM_DEPENDENCIES);
			
			List<ConfigElement> elements = dependencyConfig.getChildren(format);
			for(ConfigElement element : elements) {
				dependencies.add(element.getAttribute(PARAM_SRC));
			}
		}

		// default http parameters
		res.setContentEncoding("UTF-8");
		res.addHeader("Cache-Control", "max-age=2592000"); // 30 days
		
		// write all dependencies to output
		byte[] buf = new byte[1000];
		ServletContext context = ((WebScriptServletRequest)req).getHttpServletRequest().getSession().getServletContext();
		OutputStream output = res.getOutputStream();
		for(String path : dependencies) {
			// process suffix:
			if(suffix != null) {
				path = path.replaceFirst("\\.(\\w+)$", "-" + suffix + ".$1");
			}
			
			InputStream input = context.getResourceAsStream(path);
			if(input != null) {
				output.write(("\n/* " + path + " */\n").getBytes());
				int size = 0;
				while((size = input.read(buf, 0, 1000)) != -1) {
					output.write(buf, 0, size);
				}
				output.write('\n');
			}
		}
		
		output.flush();
		output.close();
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

}
