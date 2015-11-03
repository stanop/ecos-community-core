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
package ru.citeck.ecos.confirm;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TemplateService;

import java.io.*;
import java.util.Map;

/**
 * Author: alexander.nemerov
 * Date: 13.09.13
 */
public class ConfirmersList {

    private String templatePath;
    private TemplateService templateService;
    private ContentService contentService;

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public InputStream getConfirmersListInPDF(Map model) throws IOException {
        CharArrayWriter htmlWriter = new CharArrayWriter();
        templateService.processTemplate(templatePath, model, htmlWriter);
        ContentWriter tempHtmlWriter = contentService.getTempWriter();
        tempHtmlWriter.putContent(String.valueOf(htmlWriter.toCharArray()));
        ContentReader htmlReader = tempHtmlWriter.getReader();
        ContentWriter pdfWriter = contentService.getTempWriter();
        htmlReader.setMimetype(MimetypeMap.MIMETYPE_HTML);
        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
        contentService.transform(htmlReader, pdfWriter);
        return pdfWriter.getReader().getContentInputStream();
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
