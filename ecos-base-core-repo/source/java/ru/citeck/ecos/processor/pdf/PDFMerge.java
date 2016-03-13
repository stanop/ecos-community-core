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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopyFields;

import ru.citeck.ecos.processor.AbstractDataBundleMerge;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.DataBundleMerge;
import ru.citeck.ecos.processor.ProcessorConstants;

/**
 * PDF Merge is Data Bundle Merge, that merges different data bundles with PDF content into one Data Bundle with PDF content.
 * 
 * To merge model external Data Bundle Merge is used (specified as modelMerge).
 * 
 * @author Sergey Tiunov
 *
 */
public class PDFMerge extends AbstractDataBundleMerge
{
	private ContentService contentService;
	private DataBundleMerge modelMerge;

	@Override
	public void init() {
		this.contentService = serviceRegistry.getContentService();
	}

	@Override
	public DataBundle merge(List<DataBundle> inputs) {

		// check for null or empty list
		if(inputs == null || inputs.size() == 0) {
			return null;
		}
		
		// leave only PDFs
		List<DataBundle> pdfInputs = new ArrayList<DataBundle>(inputs.size());
		List<PdfReader> pdfReaders = new ArrayList<PdfReader>(inputs.size());
		for(DataBundle input : inputs) {
			Object mimetype = input.getModel().get(ProcessorConstants.KEY_MIMETYPE);
			if(mimetype == null || !mimetype.equals(MimetypeMap.MIMETYPE_PDF)) {
				continue;
			}
			try {
				PdfReader reader = new PdfReader(input.getInputStream());
				pdfReaders.add(reader);
			} catch (IOException e) {
				continue;
			}
			
			pdfInputs.add(input);
		}
		
		if(pdfInputs.size() == 1) {
			return pdfInputs.get(0);
		}
		
		// merge model:
		DataBundle modelBundle = modelMerge.merge(pdfInputs);

		// merge content:
		
		ContentWriter contentWriter = contentService.getTempWriter();
		OutputStream outputStream = contentWriter.getContentOutputStream();
		
        PdfCopyFields resultPdf = null;
        try {
            resultPdf = new PdfCopyFields(outputStream);
            for (PdfReader reader : pdfReaders) {
                resultPdf.addDocument(reader);
            }
            resultPdf.close();
            
            ContentReader contentReader = contentWriter.getReader();
            return helper.getDataBundle(contentReader, modelBundle.getModel());

        } catch (FileNotFoundException e) {
            return null;
        } catch (DocumentException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if(resultPdf != null) {
                resultPdf.close();
            } else if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    return null;
                }
            }
        }
	}
	
	public void setModelMerge(DataBundleMerge modelMerge) {
		this.modelMerge = modelMerge;
	}

}
