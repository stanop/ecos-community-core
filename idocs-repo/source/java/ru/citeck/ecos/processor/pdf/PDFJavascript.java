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
package ru.citeck.ecos.processor.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.DataBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PDF Javascript is a Data Bundle Line, that embeds javascript into pdf document.
 * It can be used with pdf input stream or without it, then it generates empty document and embeds javascript into it.
 * 
 * @author Sergey Tiunov
 */
public class PDFJavascript extends AbstractDataBundleLine
{
	private ContentService contentService;
	
	private String javascriptExpr;

	private static final Log logger = LogFactory.getLog(PDFJavascript.class);
	
	@Override
	public void init() {
		this.contentService = serviceRegistry.getContentService();
	}

	@Override
	public DataBundle process(DataBundle input) {
		
		InputStream inputStream = input.getInputStream();
		Map<String,Object> model = input.getModel();
		
		String javascript = (String) this.evaluateExpression(javascriptExpr, model);

		ContentWriter writer = contentService.getTempWriter();
		helper.putContentProperties(writer, model);
		OutputStream outputStream = writer.getContentOutputStream();
		
		if(inputStream != null) {
			addJavaScript(inputStream, outputStream, javascript);
		} else {
			putJavaScript(outputStream, javascript);
		}
		
		ContentReader reader = writer.getReader();
		return helper.getDataBundle(reader, model);
	}
	
	// add javascript to existing document
	public void addJavaScript(InputStream inputPdfStream, OutputStream outputPdfStream, String javascript) 
	{
		PdfReader reader = null;
		PdfStamper stamper = null;
		try {
			reader = new PdfReader(inputPdfStream);
			stamper = new PdfStamper(reader, outputPdfStream);
			
			stamper.addJavaScript(javascript);

			stamper.close();

		} catch (IOException | DocumentException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	// generate new document with javascript
	public void putJavaScript(OutputStream outputPdfStream, String javascript) {
		
		PdfCopy copy = null;
		try {
			Document document = new Document();
			copy = new PdfCopy(document, outputPdfStream);
			document.open();
			
			copy.addJavaScript(javascript);
			
			document.close();
			copy.close();
			
		} catch (DocumentException e) {
			// unlikely to get here
		}
		
	}

	/**
	 * Set javascript as an expression in a supported format.
	 * 
	 * @param javascript
	 */
	public void setJavascript(String javascript) {
		this.javascriptExpr = javascript;
	}

}
