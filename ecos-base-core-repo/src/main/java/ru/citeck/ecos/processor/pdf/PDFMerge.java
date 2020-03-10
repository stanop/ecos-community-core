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
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopyFields;

import org.apache.commons.collections.CollectionUtils;
import ru.citeck.ecos.processor.AbstractDataBundleMerge;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.DataBundleMerge;
import ru.citeck.ecos.processor.ProcessorConstants;

/**
 * PDF Merge is Data Bundle Merge, that merges different data bundles with PDF content into one Data Bundle with PDF content.
 * <p>
 * To merge model external Data Bundle Merge is used (specified as modelMerge).
 *
 * @author Sergey Tiunov
 */
@Slf4j
public class PDFMerge extends AbstractDataBundleMerge {

    private static final String MIMETYPE_IS_NOT_EQUALS_PDF = "MimeType is not equals pdf. Model: %s";
    private static final String INPUT_BUNDLE_DID_NOT_PROCESS = "Input bundle didn't process. Model: %s";
    private static final String GETTING_RESULT_PDF_ERROR = "Getting result PDF complete with errors";

    private ContentService contentService;
    private DataBundleMerge modelMerge;

    @Override
    public void init() {
        this.contentService = serviceRegistry.getContentService();
    }

    @Override
    public DataBundle merge(List<DataBundle> inputs) {
        if (CollectionUtils.isEmpty(inputs)) {
            return null;
        }

        if (inputs.size() == 1) {
            return inputs.get(0);
        }

        // leave only PDFs
        List<DataBundle> pdfInputs = new ArrayList<>(inputs.size());
        List<PdfReader> pdfReaders = new ArrayList<>(inputs.size());

        fillInputsAndReaders(inputs, pdfInputs, pdfReaders);
        return getResultPdf(pdfInputs, pdfReaders);
    }

    private void fillInputsAndReaders(List<DataBundle> inputs, List<DataBundle> pdfInputs, List<PdfReader> pdfReaders) {
        for (DataBundle input : inputs) {
            Object mimetype = input.getModel().get(ProcessorConstants.KEY_MIMETYPE);
            if (mimetype == null || !mimetype.equals(MimetypeMap.MIMETYPE_PDF)) {
                log.debug(String.format(MIMETYPE_IS_NOT_EQUALS_PDF, input.getModel().toString()));
                continue;
            }

            try {
                PdfReader reader = new PdfReader(input.getInputStream());
                pdfReaders.add(reader);
            } catch (IOException e) {
                log.debug(String.format(INPUT_BUNDLE_DID_NOT_PROCESS, input.getModel().toString()), e);
                continue;
            }

            pdfInputs.add(input);
        }
    }

    private DataBundle getResultPdf(List<DataBundle> pdfInputs, List<PdfReader> pdfReaders) {
        ContentWriter contentWriter = contentService.getTempWriter();

        try (OutputStream outputStream = contentWriter.getContentOutputStream();) {
            PdfCopyFields resultPdf = new PdfCopyFields(outputStream);
            for (PdfReader reader : pdfReaders) {
                resultPdf.addDocument(reader);
            }
            resultPdf.close();

            ContentReader contentReader = contentWriter.getReader();
            DataBundle modelBundle = modelMerge.merge(pdfInputs);
            return helper.getDataBundle(contentReader, modelBundle.getModel());
        } catch (IOException | DocumentException e) {
            log.error(GETTING_RESULT_PDF_ERROR, e);
            return null;
        }
    }

    public void setModelMerge(DataBundleMerge modelMerge) {
        this.modelMerge = modelMerge;
    }

}
