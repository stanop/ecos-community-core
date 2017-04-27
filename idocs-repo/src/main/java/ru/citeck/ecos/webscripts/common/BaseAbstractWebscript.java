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
package ru.citeck.ecos.webscripts.common;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Base Java-backed Alfresco webscript.
 * 
 * @author <a href="mailto:erik.kirs@gmail.com">Erik Kirs</a>
 */
public abstract class BaseAbstractWebscript extends AbstractWebScript {

	protected final Logger logger = Logger.getLogger(getClass());
	
	/**
	 * Provides base logging facilities and exceptions handling.
	 */
	@Override
	public void execute(WebScriptRequest aRequest, WebScriptResponse aResponse)
			throws IOException {
		
		long start = System.currentTimeMillis();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Starting " + getClass() + " webscript...");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Template variables: " + aRequest.getServiceMatch().getTemplateVars());
			logger.debug("Parameter names: " + Arrays.asList(aRequest.getParameterNames()));
		}				
		
		try {
			executeInternal(aRequest, aResponse);
		} catch (Throwable t) {
			logger.error("Error invoking webscript", t);
			if (t instanceof WebScriptException) {
				throw (WebScriptException) t;
			} else {
				throw new WebScriptException("Error invoking webscript", t);
			}
		}
		
		long end = System.currentTimeMillis();
		
		if (logger.isDebugEnabled()) {
			logger.debug(getClass() + " webscript finished, time spent(ms): " + (end - start));
		}
	}

	/**
	 * Entry point for subclasses.
	 * @param aRequest script request
	 * @param aResponse script response
	 * @throws Exception execution error
	 */
	protected abstract void executeInternal(WebScriptRequest aRequest, WebScriptResponse aResponse) throws Exception;
}
