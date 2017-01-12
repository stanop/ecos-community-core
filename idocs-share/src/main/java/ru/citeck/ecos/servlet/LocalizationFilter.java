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
/**
 *
 * THIS OVERRIDE IS USED TO EXPLICITLY SET THE ALFRESCO SHARE LOCALE TO RUSSIAN.
 * For localizing Alfresco Repository look at override of
 * org.alfresco.web.app.servlet.GlobalLocalizationFilter in Repository project
 */
package ru.citeck.ecos.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class LocalizationFilter implements Filter {

	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	
		if (request instanceof HttpServletRequest) {
			// cast the object
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			request = new StrictLocaleRequest(httpServletRequest);
		}

		// continue filter chaining
		chain.doFilter(request, response);

	}

	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to do
	}

	public void destroy() {
		// Nothing to do
	}
}
