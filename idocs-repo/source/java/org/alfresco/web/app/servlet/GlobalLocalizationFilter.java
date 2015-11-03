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
 * THIS OVERRIDE IS USED TO EXPLICITLY SET THE ALFRESCO REPOSITORY LOCALE TO RUSSIAN.
 * For localizing Share interface look at ru.citeck.LocaleRestrictor in the Share project
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.extensions.surf.util.I18NUtil;

import ru.citeck.ecos.server.utils.StrictLocaleRequest;

/**
 * @author Stas Sokolovsky
 * 
 *         Servlet filter responsible for setting a fallback default locale for
 *         ALL requests.
 */
public class GlobalLocalizationFilter implements Filter {
	private static final String LOCALE = "locale";
	public static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";

	/**
	 * Run the filter
	 * 
	 * @param request
	 *            ServletRequest
	 * @param response
	 *            ServletResponse
	 * @param chain
	 *            FilterChain
	 * @exception IOException
	 * @exception ServletException
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		// ADDON START Setting locale explicitly
		if (request instanceof HttpServletRequest) {
			// cast the object
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			request = new StrictLocaleRequest(httpServletRequest);
		}
		// ADDON END Setting locale explicitly
		
		setLanguageFromRequestHeader((HttpServletRequest) request);

		// continue filter chaining
		chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * javax.servlet.ServletResponseWrapper#setContentType(java.lang
			 * .String)
			 */
			@Override
			public void setContentType(String type) {
				super.setContentType(type);

				// Parse the parameters of the media type, since some app
				// servers (Websphere) refuse to pay attention if the
				// character encoding isn't explicitly set
				int startIndex = type.indexOf(';') + 1;
				int length = type.length();
				while (startIndex != 0 && startIndex < length) {
					int endIndex = type.indexOf(';', startIndex);
					if (endIndex == -1) {
						endIndex = length;
					}
					String param = type.substring(startIndex, endIndex);
					int sepIndex = param.indexOf('=');
					if (sepIndex != -1) {
						String name = param.substring(0, sepIndex).trim();
						if (name.equalsIgnoreCase("charset")) {
							setCharacterEncoding(param.substring(sepIndex + 1).trim());
							break;
						}
					}
					startIndex = endIndex + 1;
				}
			}
		});

	}

	/**
	 * Apply Client and Repository language locale based on the
	 * 'Accept-Language' request header
	 * 
	 * @param request
	 *            HttpServletRequest
	 */
	public void setLanguageFromRequestHeader(HttpServletRequest req) {
		Locale locale = null;

		String acceptLang = req.getHeader("Accept-Language");
		if (acceptLang != null && acceptLang.length() > 0) {
			StringTokenizer tokenizer = new StringTokenizer(acceptLang, ",; ");
			// get language and convert to java locale format
			String language = tokenizer.nextToken().replace('-', '_');
			locale = I18NUtil.parseLocale(language);
			I18NUtil.setLocale(locale);
		} else {
			I18NUtil.setLocale(Locale.getDefault());
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to do
	}

	public void destroy() {
		// Nothing to do
	}
}
