package ru.citeck.ecos.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.ProcessorHelper;

import java.util.HashMap;
import java.util.Map;

@Service
public class PDFContentTransformService {

    private final static Logger logger = Logger.getLogger(PDFContentTransformService.class);

    private NodeService nodeService;
    private ContentService contentService;
    private ProcessorHelper helper;

    public DataBundle getTransformContent(NodeRef nodeRef) {

        if (nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) == null) {
            logger.info("PDFContentTransformService. Content is null. NodeRef = " + nodeRef.toString());
            return null;
        }

        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        ContentWriter tempWriter = contentService.getTempWriter();
        tempWriter.setEncoding("UTF-8");
        tempWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);

        Map<String, Object> model = new HashMap<>();
        TransformationOptions options = new TransformationOptions();
        DataBundle transformBundle;

        if (reader.getMimetype().equals(MimetypeMap.MIMETYPE_PDF)) {
            transformBundle = helper.getDataBundle(reader, model);
        } else if (reader.getMimetype().equals("image/bmp") || reader.getMimetype().equals(MimetypeMap.MIMETYPE_IMAGE_JPEG)) {
            transformBundle = bmpAndJpegTransformToPdf(tempWriter, reader, options, model);
        } else {
            transformBundle = transformToPdf(tempWriter, reader, options, model);
        }
        return transformBundle;
    }

    private DataBundle transformToPdf(ContentWriter writer, ContentReader reader, TransformationOptions options, Map<String, Object> model) {
        try {
            contentService.transform(reader, writer, options);
        } catch (ContentIOException | NoTransformerException var6) {
            throw new IllegalStateException("Can not transform " + reader.getMimetype() + " to " + MimetypeMap.MIMETYPE_PDF, var6);
        }
        ContentReader resultReader = writer.getReader();
        return this.helper.getDataBundle(resultReader, model);
    }

    private DataBundle bmpAndJpegTransformToPdf(ContentWriter writer, ContentReader reader, TransformationOptions options, Map<String, Object> model) {
        writer.setMimetype(MimetypeMap.MIMETYPE_IMAGE_JPEG);
        try {
            contentService.transform(reader, writer, options);
        } catch (ContentIOException | NoTransformerException var6) {
            throw new IllegalStateException("Can not transform " + reader.getMimetype() + " to " + MimetypeMap.MIMETYPE_PDF, var6);
        }

        ContentReader contentJpgReader = writer.getReader();
        ContentWriter tempWriterForPdf = contentService.getTempWriter();
        tempWriterForPdf.setMimetype(MimetypeMap.MIMETYPE_PDF);
        ContentTransformer JpgTpPdfTransformer = contentService.getTransformer(contentJpgReader.getMimetype(),
                MimetypeMap.MIMETYPE_PDF);
        JpgTpPdfTransformer.transform(contentJpgReader, tempWriterForPdf);
        ContentReader resultReader = tempWriterForPdf.getReader();

        return this.helper.getDataBundle(resultReader, model);
    }

    @Autowired
    @Qualifier("DataBundleProcessorHelper")
    public void setHelper(ProcessorHelper helper) {
        this.helper = helper;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
