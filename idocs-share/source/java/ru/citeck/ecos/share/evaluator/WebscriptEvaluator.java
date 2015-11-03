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
package ru.citeck.ecos.share.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.alfresco.web.evaluator.Comparator;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebscriptEvaluator extends BaseEvaluator {

	public static final String ALFRESCO_ENDPOINT_ID = "alfresco";

	private Comparator comparator = null;
	private String accessor = null;
	private String urlTemplate = null;

	private final Logger logger = Logger.getLogger(getClass());

	@Override
	public boolean evaluate(JSONObject jsonObject) {
		final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		final String userId = rc.getUserId();
		final Connector conn;
		if (comparator == null || accessor == null || urlTemplate == null) {
			logger.error("comparator or accessor or urlTemplate is null");
			return false;
		}
		try {
			conn = rc.getServiceRegistry().getConnectorService().getConnector(
					ALFRESCO_ENDPOINT_ID,
					userId,
					ServletUtil.getSession()
			);
		} catch (ConnectorServiceException e) {
			logger.error(e.getStackTrace());
			throw new RuntimeException(e.toString());
		}
		String url = makeURL(jsonObject, urlTemplate);
		final Response response = conn.call(url);
		if (response.getStatus().getCode() == Status.STATUS_OK) {
			//do something
			Object nodeValue;
			try {
				JSONParser parser = new JSONParser();
				JSONObject object = (JSONObject) parser.parse(response.getText());
				nodeValue = getJSONValue(object, accessor);
			} catch (ParseException e) {
				logger.error(e.getStackTrace());
				return false;
			}
			return this.comparator.compare(nodeValue);
		} else {
			logger.error("Response status isn't OK");
		}
		return false;
	}

	private String makeURL(JSONObject jsonObject, String urlTemplate) {
		String result = urlTemplate;
		Pattern pattern = Pattern.compile("[\\{]([^\\{\\}]*)[\\}]");
	    Matcher matcher = pattern.matcher(result);
		while(matcher.find()) {
			result = matcher.replaceFirst((String) getJSONValue(
					jsonObject, matcher.group(1)));
			matcher = pattern.matcher(result);
		}
		return result;
	}

	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	public void setAccessor(String accessor) {
		this.accessor = accessor;
	}

	public void setUrlTemplate(String urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
}
