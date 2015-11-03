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

import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.Base64;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author: Alexander Nemerov
 * @date: 14.08.2014
 */
public class MSOffice2PdfTransformer extends AbstractContentTransformer2 {

    private String endpointId;
    private static final String SAVE_TO_TRANSFER_URI = "saveToTransfer";
    private static final String GET_STATUS_URL = "getStatus";
    private static final String GET_RESULT = "getResult";

    private static final int STATUS_IN_PROCESSING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_NOT_FOUND = 3;

    private long timeoutStatusRequest; //Milliseconds

    private static final Log logger = LogFactory.getLog(MSOffice2PdfTransformer.class);

    @Override
    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {

        InputStream is = new BufferedInputStream(reader.getContentInputStream());
        OutputStream out = writer.getContentOutputStream();
        try {
            long id = transferToConvert(is, reader.getMimetype());
            while(true) {
                if (getStatus(id) == STATUS_IN_PROCESSING) {
                    Thread.sleep(timeoutStatusRequest);
                } else if (getStatus(id) == STATUS_COMPLETED) {
                    getResult(id, out);
                    return;
                } else if (getStatus(id) == STATUS_NOT_FOUND) {
                    logger.error("Request with this id is not found on the server. id = " + id);
                    return;
                }
            }
        } finally {
            is.close();
            out.close();
        }

    }

    private long transferToConvert(InputStream input, String mimetype) throws IOException {
        Writer writer = null;
        BufferedReader result = null;
        try {
            URL url = new URL(endpointId + "/" + SAVE_TO_TRANSFER_URI);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", mimetype);
            writer = new OutputStreamWriter(connection.getOutputStream());
            byte[] nodeBytes = IOUtils.toByteArray(input);
            String nodeInBase64 = Base64.encodeBytes(nodeBytes);
            writer.write(nodeInBase64);
            writer.close();
            result = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String buffer = result.readLine();
            result.close();
            return Long.parseLong(buffer);
        } finally {
            if(writer != null) {
                writer.close();
            }
            if(result != null) {
                result.close();
            }
        }
    }

    private int getStatus(long id) throws IOException {
        BufferedReader result = null;
        try {
            URL url = new URL(endpointId + "/" + GET_STATUS_URL + "?id=" + id);
            URLConnection connection = url.openConnection();
            result = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String buffer = result.readLine();
            result.close();
            return Integer.parseInt(buffer);
        } finally {
            if(result != null) {
                result.close();
            }
        }
    }

    private void getResult(long id, OutputStream out) throws IOException {
        BufferedReader result = null;
        try {
            URL url = new URL(endpointId + "/" + GET_RESULT + "?id=" + id);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/pdf");
            result = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            byte[] decodedBytes = Base64.decode(IOUtils.toString(result));
            result.close();
            out.write(decodedBytes);
        } finally {
            if(result != null) {
                result.close();
            }
        }
    }

    /**
     * "Override" removed for compatible with 4.0.c
     */
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options) {
        return true;
    }

    /**
     * This method for compatible with 4.0.c
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
        return true;
    }

    public void setTimeoutStatusRequest(long timeoutStatusRequest) {
        this.timeoutStatusRequest = timeoutStatusRequest;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
}
