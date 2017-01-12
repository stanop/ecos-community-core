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
package ru.citeck.ecos.content.transform;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.repo.content.transform.TransformerInfoException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.RFonts;

public class Docx4jTransformer extends AbstractContentTransformer2 {

    // set up fonts
    static {
        // Windows: .*(calibri|camb|cour|arial|symb|times|Times|zapf).*
        // Mac OSX: .*(Courier New|Arial|Times New Roman|Comic Sans|Georgia|Impact|Lucida Console|Lucida Sans Unicode|Palatino Linotype|Tahoma|Trebuchet|Verdana|Symbol|Webdings|Wingdings|MS Sans Serif|MS Serif).*
        String regex = ".*(arial|times|Times|courier).*";
        PhysicalFonts.setRegex(regex);

        addFontMapping(Arrays.asList("Arial", "sans-serif"), "Arial");
        addFontMapping(Arrays.asList("Times New Roman", "Times", "serif"), "Times");
        addFontMapping(Arrays.asList("Courier New", "monospace"), "Courier");
        
    }

    private static void addFontMapping(Collection<String> cssFontFamilies, String fontName) {
        RFonts timesRFonts = Context.getWmlObjectFactory().createRFonts();
        timesRFonts.setAscii(fontName);
        timesRFonts.setHint(org.docx4j.wml.STHint.DEFAULT);
        timesRFonts.setHAnsi(fontName);
        for(String cssFontFamily : cssFontFamilies) {
            XHTMLImporterImpl.addFontMapping(cssFontFamily, timesRFonts);
        }
    }
    
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return MimetypeMap.MIMETYPE_XHTML.equals(sourceMimetype)
                && MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING.equals(targetMimetype);
    }

    @Override
    protected void transformInternal(ContentReader reader,
                                     ContentWriter writer, TransformationOptions options)
            throws Exception {
        try (
                InputStream inputStream = reader.getContentInputStream();
                OutputStream outputStream = writer.getContentOutputStream();
        ) {
            transform(inputStream, outputStream);
        }
    }

    void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
            NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
            wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
            ndp.unmarshalDefaultNumbering();

        XHTMLImporterImpl XHTMLImporter = new XHTMLImporterImpl(wordMLPackage);

        wordMLPackage.getMainDocumentPart().getContent()
                .addAll(XHTMLImporter.convert(inputStream, null));
        
        Save saver = new Save(wordMLPackage);
            saver.save(outputStream);
        } catch (Docx4JException e) {
            throw new TransformerInfoException("Can not transform html to docx", e);
        }
    }

}
