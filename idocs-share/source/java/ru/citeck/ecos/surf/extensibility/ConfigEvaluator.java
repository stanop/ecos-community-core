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
package ru.citeck.ecos.surf.extensibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.RequestContext;

import java.util.Map;

/**
 * Copy of SlingshotComponentElementEvaluator, with support for evaluating modules and global config.
 * Scoped config example: ModuleConfig/document-details/hide-document-links.
 * Global config example: /flags/client-debug
 * 
 * Example:
 * 
 * <pre>{@code
 *	<evaluator type="config.module.evaluator">
 *	    <params>
 *	        <element>/flags/client-debug</element>
 *	    </params>
 *	</evaluator>
 * }</pre>
 * 
 * @author Sergey Tiunov
 *
 */
public class ConfigEvaluator extends AbstractUniversalEvaluator
{
    private static Log logger = LogFactory.getLog(ConfigEvaluator.class);

    // Evaluator parameters
    public static final String ELEMENT = "element";
    public static final String MATCH = "match";

    protected ConfigService configService = null;

    /**
     * Sets the config service.
     *
     * @param configService the new config service
     */
    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

    /**
     * Decides if we are inside a site or not.
     *
     * @param context
     * @param params
     * @return true if we are in a site and its id matches the {@code<sites>} param (defaults to ".*")
     */
    @Override
	protected boolean evaluateImpl(RequestContext rc, Map<String, String> params)
    {
        String element = params.get(ELEMENT);
        if (element != null)
        {
            String token = null;
            String value = null;
            Config config = null;
            ConfigElement configElement = null;
            String[] tokens = element.split("/");
            int i = 0;
            for (; i < tokens.length; i++)
            {
                token = tokens[i];
                if (i == 0)
                {
                	if(token.length() > 0) {
                        config = configService.getConfig(token);
                	} else {
                		config = configService.getGlobalConfig();
                	}
                }
                else if (i == 1 && config != null)
                {
                    value = config.getConfigElementValue(token);
                    configElement = config.getConfigElement(token);
                }
                else if (i >= 2 && configElement != null)
                {
                    value = configElement.getChildValue(token);
                    configElement = configElement.getChild(token);
                }
            }
            if (value != null && i == tokens.length)
            {
                String match = params.get(MATCH);
                if (match != null)
                {
                    return match.matches(value);
                }

                // If no specific parameter instructions was provided just test if the value returns true
                return value.equalsIgnoreCase("true");
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Could not find value for <element>" + element + "</element>");
                }
            }
        }

        // No value was found
        return false;
    }

}
