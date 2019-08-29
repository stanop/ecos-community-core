package ru.citeck.ecos.template;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * This template processor allow to paste images to freemarker templates.
 * <p>
 * Images is being converted to {@link String}, consisting of
 * html data images base64 format {@link Base64TemplateImageConverter#IMAGE_SRC_FORMAT}.
 * <p>
 * <b>Be aware:</b> There is a limit on some browsers in {@code data:} length. On current implementation there is not
 * check of outbound string length and image size.
 *
 * @author Roman Makarskiy
 */
public class Base64TemplateImageConverter extends BaseTemplateProcessorExtension {

    private static final String IMAGE_SRC_FORMAT = "data:image/%s;base64,%s";

    private static final List<String> SUPPORT_IMAGES_EXTENSIONS = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png");

    private static final String QR_CODE_FORMAT = "png";
    private static final String QR_CODE_CONTENT_ENCODING = "UTF-8";

    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;

    /**
     * Convert Document {@link ContentModel#PROP_CONTENT}
     * <p>
     * Support image formats declared in {@link Base64TemplateImageConverter#SUPPORT_IMAGES_EXTENSIONS}
     */
    public String fromContent(TemplateNode templateNode) {
        if (templateNode == null || !templateNode.getExists()) {
            throw new IllegalArgumentException("TemplateNode document not exists: " + templateNode);
        }

        NodeRef document = templateNode.getNodeRef();

        ContentReader reader = contentService.getReader(document, ContentModel.PROP_CONTENT);
        if (reader == null || !reader.exists()) {
            throw new IllegalArgumentException("ContentReader not exists, nodeRef: " + document);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        reader.getContent(out);

        String extension = getExtension(document);
        String base64 = DatatypeConverter.printBase64Binary(out.toByteArray());

        return String.format(IMAGE_SRC_FORMAT, extension, base64);
    }

    private String getExtension(NodeRef document) {
        ContentData content = (ContentData) nodeService.getProperty(document, ContentModel.PROP_CONTENT);
        if (content == null) {
            throw new IllegalArgumentException(String
                    .format("Could not get extension from <%s>, because content is null", document));
        }

        String mimeType = content.getMimetype();
        if (StringUtils.isBlank(mimeType)) {
            throw new IllegalArgumentException(String
                    .format("Could not get extension from <%s>, because mimeType is blank", document));
        }

        String extension = mimetypeService.getExtension(mimeType);
        if (!SUPPORT_IMAGES_EXTENSIONS.contains(extension)) {
            throw new IllegalStateException(String.format("Content of document <%s> is an unsupported extension <%s>",
                    document, extension));
        }

        return extension;
    }

    /**
     * Convert generated QR code.
     *
     * @param content QR code content
     * @param width   QR code width
     * @param height  QR code height
     */
    public String fromQrCode(String content, int width, int height) {
        BitMatrix matrix;
        com.google.zxing.Writer writer = new MultiFormatWriter();

        try {
            Hashtable<EncodeHintType, String> hints = new Hashtable<>(1);
            hints.put(EncodeHintType.CHARACTER_SET, QR_CODE_CONTENT_ENCODING);
            matrix = writer.encode(content,
                    BarcodeFormat.QR_CODE, width, height, hints);
        } catch (com.google.zxing.WriterException e) {
            throw new RuntimeException("Error encode QR code", e);
        }

        ByteArrayOutputStream out;
        try {
            out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, QR_CODE_FORMAT, out);
        } catch (IOException e) {
            throw new RuntimeException("Error encode QR code", e);
        }

        String base64 = DatatypeConverter.printBase64Binary(out.toByteArray());
        return String.format(IMAGE_SRC_FORMAT, QR_CODE_FORMAT, base64);
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Autowired
    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }
}
