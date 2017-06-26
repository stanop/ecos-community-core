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

package ru.citeck.ecos.surf.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Nemerov
 * @date 03.12.13
 */
public class ResourceResolver extends ServletContextResourcePatternResolver {

	private static Log logger = LogFactory.getLog(ResourceResolver.class);

	public ResourceResolver() {
		super(new DefaultResourceLoader());
	}

	public ResourceResolver(ResourceLoader resourceLoader)
	{
		super(resourceLoader);
	}

	public List<String> getResources(String... locationPatterns) throws IOException
	{
		logger.debug("Got location patterns: " + locationPatterns);
		LinkedList<String> result = new LinkedList<String>();
		for (String locationPattern : locationPatterns)
		{
			Resource[] resourcesForPattern = getResources(locationPattern);
			for (Resource resourceForPattern : resourcesForPattern) {
				String url = resourceForPattern.getURL().toString();
				//exclude resources from source and target folders because a same files deploying multiple times
				//TODO: verify the uniqueness of resources and load them from any classpath
				if (!url.contains("/src/main/resources/") && !url.contains("/target/classes/")) {
					result.add(url);
				}
			}
		}
		logger.debug("Found resource locations: " + result);
		return result;
	}
}
