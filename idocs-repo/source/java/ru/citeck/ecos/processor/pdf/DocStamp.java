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

import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.BaseFont;

import ru.citeck.ecos.processor.CompositeDataBundleProcessor;
import ru.citeck.ecos.processor.DataBundleProcessor;
import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.transform.AffineTransformCalculator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Document Stamp is Data Bundle Decorator (Line), that creates new page and puts stamp and document id on it.
 * To produce the stamp an external Data Bundle Processor is used.
 * To calculate the position of stamp on the page an external Affine Transform Calculator is used.
 * 
 * @author Zaripova Elena
 *
 */
public class DocStamp extends AbstractDataBundleLine
{
	private ContentService contentService;
	private AffineTransformCalculator transformCalculator;
	
	private DataBundleProcessor stampProcessor;
	private List<DataBundleProcessor> stampProcessors;
	private Boolean foreground;
	private String appendStringTo;
	private String appendStringFrom;
	private String appendIDFrom;
	private String appendIDTo;
	private float leftMargin;
    private static final Log logger = LogFactory.getLog(DocStamp.class);
	
	public void init() {
		this.contentService = serviceRegistry.getContentService();
		if(stampProcessor instanceof CompositeDataBundleProcessor) {
			((CompositeDataBundleProcessor)stampProcessor).setProcessors(stampProcessors);
		}
	}
	
	@Override
	public DataBundle process(DataBundle input) {

		// get main stream:
		InputStream pdfInputStream = input.getInputStream();
		String appendIDFrom = super.evaluateExpression(appendStringFrom, input.getModel()).toString();
		String appendIDTo = super.evaluateExpression(appendStringTo, input.getModel()).toString();

		// get stamp stream:
		List<DataBundle> stampInputs = new ArrayList<DataBundle>(1);
		stampInputs.add(new DataBundle(input.getModel()));
		List<DataBundle> stampOutputs = stampProcessor.process(stampInputs);
		
		InputStream pdfStampStream = null;
		if(stampOutputs.size() > 0) {
			pdfStampStream = stampOutputs.get(0).getInputStream();
		} else {
			return input;
		}

		// do actual stamping
		InputStream pdfStampedStream = stampPDF(pdfInputStream, pdfStampStream, appendIDFrom, appendIDTo);
		
		return new DataBundle(input, pdfStampedStream);
	}

	// stamp pdf with stamp
	private InputStream stampPDF(InputStream pdfInputStream, InputStream pdfStampStream, String appendIDFrom, String appendIDTo) {

		PdfReader reader = null;
		PdfReader stampReader = null;
		PdfStamper stamper = null;
		
		ContentWriter contentWriter = contentService.getTempWriter();
		
		OutputStream pdfStampedOutputStream = contentWriter.getContentOutputStream();
		try {
			reader = new PdfReader(pdfInputStream);
			stamper = new PdfStamper(reader, pdfStampedOutputStream);
			stampReader = new PdfReader(pdfStampStream);

			PdfImportedPage stamp = stamper.getImportedPage(stampReader, 1);
			Rectangle stampSize = stampReader.getPageSize(1);

			int numOfPages = 1;
			int from = Integer.parseInt(appendIDFrom);
			int to = Integer.parseInt(appendIDTo);
			for (int i = from; i <= to; i++) {
				// calculate affine transform:
				stamper.insertPage(numOfPages,reader.getPageSizeWithRotation(numOfPages));
				Rectangle pageSize = reader.getPageSize(numOfPages);
				// get transformation matrix
				double[] m = new double[6];
				AffineTransform transform = transformCalculator.calculate(stampSize.getWidth(), stampSize.getHeight(), pageSize.getWidth(), pageSize.getHeight());
				transform.getMatrix(m);

				PdfContentByte page = foreground ? stamper.getOverContent(numOfPages) : stamper.getUnderContent(numOfPages);
				BaseFont bf = BaseFont.createFont();
				page.beginText();
				page.setFontAndSize(bf, 16);
				page.setTextMatrix((float) m[0], (float) m[1], (float) m[2], (float) m[3], leftMargin, (float) m[5]);
				page.showText("ID: "+i);
				page.endText();
				page.addTemplate(stamp, (float) m[0], (float) m[1], (float) m[2], (float) m[3], (float) m[4], (float) m[5]);
				numOfPages++;

			}
		} catch (Exception e) {
			throw new AlfrescoRuntimeException("Caught exception", e);
		} finally {
			if(reader != null) {
				reader.close();
			}
			if(stampReader != null) {
				stampReader.close();
			}
			if(stamper != null) {
				try {
					stamper.close();
				} catch (Exception e) {
					throw new AlfrescoRuntimeException("Caught exception", e);
				}
			}
		}
		ContentReader contentReader = contentWriter.getReader();
		if(contentReader != null && contentReader.exists()) {
			return contentReader.getContentInputStream();
		} else {
			return null;
		}
	}

	
	/**
	 * Set affine transform calculator.
	 * It is used to calculate stamp position on the page.
	 * 
	 * @param transformCalculator
	 */
	public void setTransformCalculator(AffineTransformCalculator transformCalculator) {
		this.transformCalculator = transformCalculator;
	}

	/**
	 * Set Data Bundle Processor, that is used to produce stamp.
	 * 
	 * @param stampProcessor
	 */
	public void setStampProcessor(DataBundleProcessor stampProcessor) {
		this.stampProcessor = stampProcessor;
	}

	public void setStampProcessors(List<DataBundleProcessor> stampProcessors) {
		this.stampProcessors = stampProcessors;
	}

	/**
	 * Specify if the stamp should be set above the page (foreground = true), or under it (foreground = false).
	 * 
	 * @param foreground
	 */
	public void setForeground(Boolean foreground) {
		this.foreground = foreground;
	}

	/**
	 * Specify if the stamp should be set above the page (foreground = true), or under it (foreground = false).
	 * 
	 * @param foreground
	 */
	public void setAppendStringFrom(String appendStringFrom) {
		this.appendStringFrom = appendStringFrom;
	}
	/**
	 * Specify if the stamp should be set above the page (foreground = true), or under it (foreground = false).
	 * 
	 * @param foreground
	 */
	public void setAppendStringTo(String appendStringTo) {
		this.appendStringTo = appendStringTo;
	}
	
	/**
	 * Set left margin.
	 * 
	 * @param leftMargin
	 */
	public void setLeftMargin(float leftMargin) {
		this.leftMargin = leftMargin;
	}

}
