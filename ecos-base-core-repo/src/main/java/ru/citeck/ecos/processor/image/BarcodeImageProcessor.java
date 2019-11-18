package ru.citeck.ecos.processor.image;

import com.google.zxing.BarcodeFormat;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.barcode.BarcodeAttributeRegistry;
import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.BarcodeProcessor;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.exception.BarcodeInputException;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.template.Base64TemplateImageConverter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BarcodeImageProcessor extends AbstractDataBundleLine implements ApplicationContextAware {

    private ContentService contentService;
    private BarcodeAttributeRegistry barcodeAttributeRegistry;
    private ApplicationContext applicationContext;
    private Base64TemplateImageConverter converter;

    private String barcodeName;
    private String barcodeWidth;
    private String barcodeHeight;
    private String nodeProperty;

    public void init() {
        this.contentService = serviceRegistry.getContentService();
    }

    @SuppressWarnings("unchecked")
    private void handleProperty(Map<String, Object> model) {
        try {
            Object argsObj = model.get("args");
            if (argsObj instanceof HashMap) {
                Map<String, String> args = (HashMap<String, String>) argsObj;
                args.computeIfAbsent("property", e -> {
                    String nodeRef = args.get("nodeRef");
                    RecordRef recordRef = RecordRef.create("", nodeRef);
                    return barcodeAttributeRegistry.getAttribute(recordRef);
                });
            }
        } catch (ClassCastException cce) {
            log.error("Unable to put 'property' in request's params. " + cce.getLocalizedMessage());
        }
    }

    @Override
    public DataBundle process(DataBundle input) {

        Map<String, Object> model = input.getModel();

        handleProperty(model);

        ContentWriter writer = contentService.getTempWriter();
        writer.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);

//        saveImage(writer, model);

        int width = 0;
        if (barcodeWidth != null) {
            width = Integer.parseInt((String) super.evaluateExpression(barcodeWidth, model));
        }

        int height = 0;
        if (barcodeHeight != null) {
            height = Integer.parseInt((String) super.evaluateExpression(barcodeHeight, model));
        }

        String barcodeInput;
        try {
            barcodeInput = super.evaluateExpression(nodeProperty, model).toString();
        } catch (Exception e) {
            throw new BarcodeInputException();
        }

        String result = converter.fromBarcode(barcodeInput, width, height, BarcodeFormat.CODE_128);
        return helper.getDataBundle(writer.getReader(), model);
    }

    @Autowired
    public void setBarcodeAttributeRegistry(BarcodeAttributeRegistry barcodeAttributeRegistry) {
        this.barcodeAttributeRegistry = barcodeAttributeRegistry;
    }

    @Autowired
    public void setConverter(Base64TemplateImageConverter converter) {
        this.converter = converter;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setBarcodeName(String barcodeName) {
        this.barcodeName = barcodeName;
    }

    public void setNodeProperty(String nodeProperty) {
        this.nodeProperty = nodeProperty;
    }

    public void setBarcodeWidth(String barcodeWidth) {
        this.barcodeWidth = barcodeWidth;
    }

    public void setBarcodeHeight(String barcodeHeight) {
        this.barcodeHeight = barcodeHeight;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
}
