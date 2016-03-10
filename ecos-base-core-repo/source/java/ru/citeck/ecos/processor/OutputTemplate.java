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
package ru.citeck.ecos.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TemplateService;

/**
 * Output Template is a Data Bundle Generator, that processes the specified template with a Data Bundle model.
 * Any template processor can be used, mimetype and encoding of output depend on it.
 * 
 * Engine and template can be specified as expressions, supported by expression evaluator.
 * 
 * @author Sergey Tiunov
 *
 */
public class OutputTemplate extends AbstractDataBundleLine {

	private ContentService contentService;
	private TemplateService templateService;
	private String engineExpr;
	private String templateExpr;
	private String encoding;
	private String mimetype;
	
	@Override
	public void init() {
		this.contentService = serviceRegistry.getContentService();
		this.templateService = serviceRegistry.getTemplateService();
	}
	
	@Override
	public DataBundle process(DataBundle input) {
		
		Map<String,Object> model = input.needModel();
		
		ContentWriter contentWriter = contentService.getTempWriter();
		contentWriter.setEncoding(evaluateExpression(encoding, model).toString());
		contentWriter.setMimetype(evaluateExpression(mimetype, model).toString());
		OutputStreamWriter writer = null;

		try {
			writer = new OutputStreamWriter(contentWriter.getContentOutputStream(), contentWriter.getEncoding());
		
			String template = (String) evaluateExpression(templateExpr, model);

			processTemplate(engineExpr, template, model, writer);
		} catch (UnsupportedEncodingException e) {
			throw new AlfrescoRuntimeException("OutputStreamWriter is not created with encoding: " + contentWriter.getEncoding(), e);
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new AlfrescoRuntimeException("OutputTemplate processor: couldn't close writer", e);
				}
			}
		}

		try {
			InputStream inputStream = input.getInputStream();
			if(inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			//do nothing
		}
		
		ContentReader resultReader = contentWriter.getReader();
		return helper.getDataBundle(resultReader, model);
	}

	private void processTemplate(String engineExpr, String template, Map<String, Object> model, OutputStreamWriter writer) {
		if(engineExpr != null) {
			String engine = (String) evaluateExpression(engineExpr, model);
			if(engine != null && !engine.isEmpty()) {
				templateService.processTemplate(engine, template, model, writer);
				return;
			}
		}
		templateService.processTemplate(template, model, writer);
	}

	/**
	 * Set template engine.
	 * It can be specified as expression in the supported format.
	 * @param engine
	 */
	public void setEngine(String engine) {
		this.engineExpr = engine;
	}

	/**
	 * Set template.
	 * It can be specified as expression in the supported format.
	 * @param engine
	 */
	public void setTemplate(String template) {
		this.templateExpr = template;
	}

	/**
	 * Set template output encoding
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set template output mimetype
	 * @param encoding
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
}
