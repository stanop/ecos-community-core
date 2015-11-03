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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.w3c.tidy.Tidy;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author alexander.nemerov
 * date 31.07.2015.
 */
public class Html2XhtmlTransformer extends AbstractContentTransformer2 {

    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return MimetypeMap.MIMETYPE_HTML.equals(sourceMimetype)
                && MimetypeMap.MIMETYPE_XHTML.equals(targetMimetype);
    }

    @Override
    protected void transformInternal(ContentReader reader, ContentWriter writer,
                                     TransformationOptions transformationOptions) throws Exception {
        try (
                InputStream inputStream = reader.getContentInputStream();
                OutputStream outputStream = writer.getContentOutputStream();
        ) {
            transform(inputStream, outputStream);
        }

    }

    void transform(InputStream inputStream, OutputStream outputStream) {
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setInputEncoding("UTF-8");
        tidy.setOutputEncoding("UTF-8");
        tidy.parse(inputStream, outputStream);
    }
}
