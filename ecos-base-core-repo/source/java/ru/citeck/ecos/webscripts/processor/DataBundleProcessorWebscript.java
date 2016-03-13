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
package ru.citeck.ecos.webscripts.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.springframework.extensions.webscripts.json.JSONUtils;
import ru.citeck.ecos.processor.CompositeDataBundleProcessor;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.DataBundleProcessor;
import ru.citeck.ecos.processor.ExpressionEvaluator;
import ru.citeck.ecos.processor.ProcessorConstants;
import ru.citeck.ecos.server.utils.Utils;
import ru.citeck.ecos.webscripts.utils.WebScriptUtils;
import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Web Script, that executes specified data bundle processors and returns the result.
 * It puts web script arguments as object "args" in the input model.
 * It utilize output variables @{code ProcessorConstants.MIMETYPE}, 
 *  @{code ProcessorConstants.ENCODING} and @{code ProcessorConstants.FILENAME}
 *  to set corresponding response properties.
 * 
 * @author Sergey Tiunov
 *
 */
public class DataBundleProcessorWebscript extends AbstractWebScript
{
	public static final String KEY_ARGS = "args";

    private static final String KEY_JSON = "jsonUtils";

    private DataBundleProcessor processor;
	private List<DataBundleProcessor> processors;
	private ExpressionEvaluator evaluator;
	private String downloadExpr;
	private MimetypeService mimetypeService;
	
	public void init() {
		if(processor instanceof CompositeDataBundleProcessor) {
			((CompositeDataBundleProcessor)processor).setProcessors(processors);
		}
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException 
	{
		DataBundle inputBundle = getInputBundle(req);
		
		List<DataBundle> inputs = Arrays.asList(new DataBundle[]{inputBundle});

		// get actual input stream and model
		InputStream inputStream = null;

		OutputStream outputStream = null;
		
		try {
			// do the processing
			List<DataBundle> outputs = processor.process(inputs);

			Map<String,Object> outputModel = null;
			for(DataBundle output : outputs) {
				if(output == null) continue;
				InputStream str = output.getInputStream();
				if(str != null) {
					inputStream = str;
					outputModel = output.getModel();
					break;
				}
			}
			
			if(inputStream != null) {
				
				// first set the headers
				String encoding = (String) outputModel.get(ProcessorConstants.KEY_ENCODING);
				String mimetype = (String) outputModel.get(ProcessorConstants.KEY_MIMETYPE);
				String filename = (String) outputModel.get(ProcessorConstants.KEY_FILENAME);

				if(encoding != null) {
					res.setContentEncoding(encoding);
				}
				if(mimetype != null) {
					res.setContentType(mimetype);
				}
				if(filename == null) {
					filename = super.getDescription().getId();
				}
				if(mimetype != null) {
					String extension = mimetypeService.getExtension(mimetype);
					if(filename!=null && extension!=null && !filename.endsWith(extension))
					{
						filename=filename+"."+extension;
					}
				}

				String download = (String) evaluator.evaluate(downloadExpr, outputModel);
		        res.setHeader("Content-Disposition",
		                Utils.encodeContentDispositionForDownload(req, filename, "", "false".equals(download)));

				// then output the content
				outputStream = res.getOutputStream();
				
				byte[] buffer = new byte[1000];
				while(true) {
					int size = inputStream.read(buffer);
					if(size <= 0) break;
					outputStream.write(buffer, 0, size);
				}
				
				outputStream.flush();
				
			}
			
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
			if(outputStream != null) {
				outputStream.close();
			}
		}
		
	}

	private DataBundle getInputBundle(WebScriptRequest req) {
		Content content = req.getContent();
        InputStream stream = content.getInputStream();
		Map<String,Object> model = new HashMap<String,Object>(2);
		model.put(KEY_ARGS, WebScriptUtils.getParameterMap(req));
		model.put(KEY_JSON, new JSONUtils());
		model.put(ProcessorConstants.KEY_ENCODING, content.getEncoding());
		model.put(ProcessorConstants.KEY_MIMETYPE, content.getMimetype());
        DataBundle inputBundle = new DataBundle(stream, model);
		return inputBundle;
	}
	
	public void setProcessor(DataBundleProcessor processor) {
		this.processor = processor;
	}

	public void setProcessors(List<DataBundleProcessor> processors) {
		this.processors = processors;
	}

	public void setDownload(String downloadExpr) {
		this.downloadExpr = downloadExpr;
	}

	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	/**
	 * @return the mimetypeService
	 */
	public MimetypeService getMimetypeService() {
		return mimetypeService;
	}

	/**
	 * @param mimetypeService the mimetypeService to set
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

}
