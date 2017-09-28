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

import java.util.Map;

import org.alfresco.service.cmr.repository.*;

/**
 * Transform content is a Data Bundle Line, that transforms given content from any supported format to the format specified.
 * Internally Content Service Transformers are used to perform the transformation.
 * If no transformer can be found, exception is thrown.
 * 
 * Output mimetype and encoding can be specified as expressions, supported by expression evaluator.
 * 
 * @author Sergey Tiunov
 *
 */
public class TransformContent extends AbstractDataBundleLine
{
	private ContentService contentService;
	private String outputMimetype;
	private String outputEncoding;
	private TransformationOptions options;
	
	@Override
	public void init() {
		this.contentService = serviceRegistry.getContentService();
		if(options == null) options = new TransformationOptions();
	}
	
	@Override
	public DataBundle process(DataBundle input) {
		
		Map<String,Object> model = input.needModel();

		ContentReader reader = helper.getContentReader(input);
		
		ContentWriter writer = contentService.getTempWriter();
		writer.setEncoding(evaluateExpression(outputEncoding, model).toString());
		writer.setMimetype(evaluateExpression(outputMimetype, model).toString());
		
		try {
			contentService.transform(reader, writer, options);
		} catch (NoTransformerException | ContentIOException e) {
			throw new IllegalStateException("Can not transform " + reader.getMimetype() + " to " + outputMimetype, e);
		}

		ContentReader resultReader = writer.getReader();
		return helper.getDataBundle(resultReader, model);
	}
	
	public void setOutputMimetype(String outputMimetype) {
		this.outputMimetype = outputMimetype;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setOptions(TransformationOptions options) {
		this.options = options;
	}

}
