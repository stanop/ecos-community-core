package ru.citeck.ecos.template;

import com.google.zxing.BarcodeFormat;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.barcode.BarcodeService;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
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

    private static final List<String> SUPPORT_IMAGES_EXTENSIONS = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png");

    private static final String IMAGE_SRC_FORMAT = "data:image/%s;base64,%s";
    private static final String PNG_IMAGE_FORMAT = "png";

    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private BarcodeService barcodeService;

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
        String base64 = barcodeService.getBarcodeAsBase64FromContent(content, width, height, BarcodeFormat.QR_CODE);
        return String.format(IMAGE_SRC_FORMAT, PNG_IMAGE_FORMAT, base64);
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

    @Autowired
    public void setBarcodeService(BarcodeService barcodeService) {
        this.barcodeService = barcodeService;
    }
}
