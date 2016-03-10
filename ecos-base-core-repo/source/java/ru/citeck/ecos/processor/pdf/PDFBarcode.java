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
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;


import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.DataBundle;

/**
 * PDF Barcode is a Data Bundle Generator, that is used to generate specified barcode.
 * 
 * iText barcodes are used.
 * As the barcode object should contain the code, we can not use one barcode object to generate all barcodes.
 * That is why spring prototypes are used: barcode prototype should be defined in spring context and specified by barcodeName here.
 * 
 * Barcode Input can be specified as expression, supported by expression evaluator.
 * 
 * @author Sergey Tiunov
 *
 */
public class PDFBarcode extends AbstractDataBundleLine implements ApplicationContextAware
{
	private ContentService contentService;
	
	private String barcodeNameExpr;
	private String barcodeInputExpr;
	private String marginsExpr;
	private String scaleFactorExpr;
	private ApplicationContext applicationContext;
	
	public void init() {
		this.contentService = serviceRegistry.getContentService();
	}

	@Override
	public DataBundle process(DataBundle input) {

		// get input model
		Map<String,Object> model = input.getModel();
		
		// get temp writer
		ContentWriter writer = contentService.getTempWriter();
		writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
		
		// print barcode to temp file
		printBarcode(writer.getContentOutputStream(), model);
		
		return helper.getDataBundle(writer.getReader(), model);
	}

	private void printBarcode(OutputStream outputStream, Map<String,Object> model) {
		
	    // get barcode name
	    String barcodeName = super.evaluateExpression(barcodeNameExpr, model).toString();
	    
        // generate barcode input
        String barcodeInput = super.evaluateExpression(barcodeInputExpr, model).toString();
        
        float scaleFactor = 1.0f;
        if(scaleFactorExpr != null) {
            scaleFactor = Float.parseFloat((String) super.evaluateExpression(scaleFactorExpr, model));
        }
        
		try {
			Barcode barcode = applicationContext.getBean(barcodeName, Barcode.class);
	        barcode.setCode(barcodeInput);
	        
			Document document = null;
			
			Rectangle barcodeSize = barcode.getBarcodeSize();
			float marginLeft = 0, 
			    marginRight = 0, 
			    marginTop = 0, 
			    marginBottom = 0, 
			    barcodeWidth = barcodeSize.getWidth() * scaleFactor, 
			    barcodeHeight = barcodeSize.getHeight() * scaleFactor;
			if(marginsExpr != null) {
			    String marginsString = (String) super.evaluateExpression(marginsExpr, model);
			    String[] marginStrings = marginsString.split(",");
                if(marginStrings.length > 0) marginLeft = Float.parseFloat(marginStrings[0]);
                if(marginStrings.length > 1) marginRight = Float.parseFloat(marginStrings[1]);
                if(marginStrings.length > 2) marginTop = Float.parseFloat(marginStrings[2]);
                if(marginStrings.length > 3) marginBottom = Float.parseFloat(marginStrings[3]);
			}
			document = new Document(new Rectangle(barcodeWidth + marginLeft + marginRight, barcodeHeight + marginTop + marginBottom));
			document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
			
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
	        PdfContentByte cb = new PdfContentByte(writer);
			
	        Image barcodeImage = barcode.createImageWithBarcode(cb, null, null);
	        barcodeImage.scaleAbsolute(barcodeWidth, barcodeHeight);
	        document.add(barcodeImage);
			document.close();
			
		} catch (DocumentException e) {
			// do nothing
		} finally {
			if(outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
		
	}

	/**
	 * Set the barcode name.
	 * It should be the name of prototype bean, that contains all common properties of the iText barcode (all, except the code).
	 * 
	 * @param barcodeName
	 */
	public void setBarcodeName(String barcodeName) {
		this.barcodeNameExpr = barcodeName;
	}

	/**
	 * Set the barcode input.
	 * It is used as "code" parameter of the barcode.
	 * It can be specified as an expression of supported format.
	 * 
	 * @param barcodeInput
	 */
	public void setBarcodeInput(String barcodeInput) {
		this.barcodeInputExpr = barcodeInput;
	}

    /**
     * Set margins expression.
     * This expression should evaluate to string of comma-separated numbers for "left,right,top,bottom" margins.
     * 
     * @param marginsExpr
     */
    public void setMargins(String marginsExpr) {
        this.marginsExpr = marginsExpr;
    }
    
    /**
     * Set barcode scale factor.
     * 
     * @param margins
     */
    public void setScaleFactor(String scaleFactor) {
        this.scaleFactorExpr = scaleFactor;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) 
			throws BeansException 
	{
		this.applicationContext = applicationContext;
	}

}
